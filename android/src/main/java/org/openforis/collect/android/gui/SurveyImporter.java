
package org.openforis.collect.android.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.util.Unzipper;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.commons.versioning.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SurveyImporter {
    public static final String DATABASE_NAME = "collect.db";
    public static final String INFO_PROPERTIES_NAME = "info.properties";
    public static final String SELECTED_SURVEY = "org.openforis.collect.android.SelectedSurvey";
    private static final String SURVEY_FILE_EXTENSION = "collect-mobile";
    private final String sourceSurveyPath;
    private final Context context;

    public SurveyImporter(String sourceSurveyPath, Context context) {
        this.sourceSurveyPath = sourceSurveyPath;
        this.context = context;
    }

    public boolean importSurvey(boolean overwrite) throws MalformedSurvey, WrongSurveyVersion, UnsupportedFileType {
        checkSupportedFileType();
        try {
            File tempDir = unzipSurveyDefinition();
            String sourceSurveyDatabasePath = new File(tempDir, DATABASE_NAME).getAbsolutePath();
            SurveyBackupInfo info = info(tempDir);
            String surveyName = info.getSurveyName();

            File databases = AppDirs.surveyDatabasesDir(surveyName, context);
            File targetSurveyDatabase = new File(databases, ServiceLocator.MODEL_DB);

            if (!overwrite && targetSurveyDatabase.exists())
                return false;

            realityCheckDatabaseToImport(sourceSurveyDatabasePath);
            Version version = getAndVerifyVersion(info);
            ServiceLocator.deleteNodeDatabase(context, surveyName);
            ServiceLocator.deleteModelDatabase(context, surveyName);

            File imagesDir = AppDirs.surveyImagesDir(surveyName, context);
            if (imagesDir.exists())
                FileUtils.deleteDirectory(imagesDir);

            FileUtils.copyFile(new File(sourceSurveyDatabasePath), targetSurveyDatabase);
            FileUtils.deleteDirectory(tempDir);
            migrateIfNeeded(version, targetSurveyDatabase, surveyName);
            selectSurvey(surveyName, context);
            AndroidFiles.makeDiscoverable(targetSurveyDatabase, context);
            return true;
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        } catch (SQLiteException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        }
    }

    private void checkSupportedFileType() throws UnsupportedFileType {
        String foundExtension = FilenameUtils.getExtension(sourceSurveyPath);
        if (!SURVEY_FILE_EXTENSION.equalsIgnoreCase(foundExtension)) {
            throw new UnsupportedFileType(SURVEY_FILE_EXTENSION, foundExtension);
        }
    }

    private void realityCheckDatabaseToImport(String sourceSurveyDatabasePath) {
        SQLiteDatabase database = context.openOrCreateDatabase(sourceSurveyDatabasePath, 0, null);
        database.close();
    }

    public static String selectedSurvey(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_SURVEY, null);
    }

    public static void selectSurvey(String surveyName, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_SURVEY, surveyName);
        editor.commit();
        ServiceLocator.reset(context);
    }

    private void migrateIfNeeded(Version surveyAppVersion, File targetSurveyDatabase, String surveyName) {
        if (surveyAppVersion.compareTo(Collect.VERSION, Version.Significance.MINOR) < 0) {
            AndroidDatabase database = new AndroidDatabase(context, targetSurveyDatabase);
            new ModelDatabaseMigrator(database, surveyName, context).migrate();
        }
    }

    public static void importDefaultSurvey(Context context) {
        try {
            File tempDir = org.openforis.collect.android.util.FileUtils.createTempDir();
            InputStream sourceInput = SurveyImporter.class.getResourceAsStream("/demo.collect-mobile");
            File intermediateSurveyPath = new File(tempDir, "demo.collect-mobile");
            FileOutputStream intermediateOutput = new FileOutputStream(intermediateSurveyPath);
            IOUtils.copy(sourceInput, intermediateOutput);
            new SurveyImporter(intermediateSurveyPath.getAbsolutePath(), context).importSurvey(true);
            FileUtils.deleteDirectory(tempDir);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Version getAndVerifyVersion(SurveyBackupInfo info) {
        Version surveyVer = info.getCollectVersion();
        Version appVer = Collect.VERSION;
        if (surveyVer.compareTo(appVer, Version.Significance.MINOR) > 0)
            throw new WrongSurveyVersion(sourceSurveyPath, surveyVer);
        return surveyVer;
    }

    private SurveyBackupInfo info(File dir) {
        File infoPropertiesFile = new File(dir, INFO_PROPERTIES_NAME);
        SurveyBackupInfo info;
        FileInputStream is = null;
        try {
            is = new FileInputStream(infoPropertiesFile);
            info = SurveyBackupInfo.parse(is);
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return info;
    }

    private File unzipSurveyDefinition() throws IOException {
        File folder = org.openforis.collect.android.util.FileUtils.createTempDir();
        File zipFile = new File(sourceSurveyPath);
        if (!zipFile.exists())
            throw new FileNotFoundException("File not found: " + sourceSurveyPath);
        try {
            new Unzipper(zipFile, folder).unzip(DATABASE_NAME, INFO_PROPERTIES_NAME);
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        }

        return folder;
    }

    public static String surveyMinorVersion(Version version) {
        return version.getMajor() + "." + version.getMinor() + ".x";
    }
}
