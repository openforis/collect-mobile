package org.openforis.collect.android.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.util.Unzipper;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.commons.versioning.Version;

import java.io.*;

public class SurveyImporter {
    public static final String DATABASE_NAME = "collect.db";
    public static final String INFO_PROPERTIES_NAME = "info.properties";
    public static final String SELECTED_SURVEY = "org.openforis.collect.android.SelectedSurvey";
    private final String sourceSurveyPath;
    private final Context applicationContext;

    public SurveyImporter(String sourceSurveyPath, Context context) {
        this.sourceSurveyPath = sourceSurveyPath;
        this.applicationContext = context;
    }

    public void importSurvey() throws MalformedSurvey, WrongSurveyVersion {
        try {
            File tempDir = unzipSurveyDefinition(sourceSurveyPath);
            String sourceSurveyDatabasePath = new File(tempDir, DATABASE_NAME).getAbsolutePath();
            realityCheckDatabaseToImport(sourceSurveyDatabasePath);
            SurveyBackupInfo info = info(tempDir);
            String surveyName = info.getSurveyName();
            Version version = getAndVerifyVersion(info);
            ServiceLocator.deleteNodeDatabase(applicationContext, surveyName);
            ServiceLocator.deleteModelDatabase(applicationContext, surveyName);

            File databases = AppDirs.surveyDatabasesDir(surveyName, applicationContext);
            String targetSurveyDatabasePath = new File(databases, ServiceLocator.MODEL_DB).getAbsolutePath();
            File targetSurveyDatabase = applicationContext.getDatabasePath(targetSurveyDatabasePath);
            FileUtils.copyFile(new File(sourceSurveyDatabasePath), targetSurveyDatabase);
            FileUtils.deleteDirectory(tempDir);
            migrateIfNeeded(version, targetSurveyDatabase);
            selectSurvey(surveyName, applicationContext);
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        } catch (SQLiteException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        }
    }

    private void realityCheckDatabaseToImport(String sourceSurveyDatabasePath) {
        SQLiteDatabase database = applicationContext.openOrCreateDatabase(sourceSurveyDatabasePath, 0, null);
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

    private void migrateIfNeeded(Version version, File targetSurveyDatabase) {
        AndroidDatabase database = new AndroidDatabase(applicationContext, targetSurveyDatabase);
        Version currentVersion = Collect.getVersion();
        if (version.getMajor() < currentVersion.getMajor() || version.getMinor() < currentVersion.getMinor())
            new ModelDatabaseMigrator(database, applicationContext).migrate();
    }


    public static void importDefaultSurvey(Context context) {
        try {
            File tempDir = createTempDir();
            InputStream sourceInput = SurveyImporter.class.getResourceAsStream("/demo.collect-mobile");
            File intermediateSurveyPath = new File(tempDir, "demo.collect-mobile");
            FileOutputStream intermediateOutput = new FileOutputStream(intermediateSurveyPath);
            IOUtils.copy(sourceInput, intermediateOutput);
            new SurveyImporter(intermediateSurveyPath.getAbsolutePath(), context).importSurvey();
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private Version getAndVerifyVersion(SurveyBackupInfo info) {
        Version version = info.getCollectVersion();
        Version currentVersion = Collect.getVersion();
        if (version.getMajor() > currentVersion.getMajor() || version.getMinor() > currentVersion.getMinor())
            throw new WrongSurveyVersion(sourceSurveyPath, version);
        return version;

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
            close(is);
        }
        return info;
    }

    private File unzipSurveyDefinition(String surveyDatabasePath) throws IOException {
        File folder = createTempDir();
        File zipFile = new File(surveyDatabasePath);
        if (!zipFile.exists())
            throw new FileNotFoundException("File not found: " + surveyDatabasePath);
        try {
            new Unzipper(zipFile, folder).unzip(DATABASE_NAME, INFO_PROPERTIES_NAME);
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        }

        return folder;
    }

    private static File createTempDir() throws IOException {
        File tempDir = File.createTempFile("collect", Long.toString((System.nanoTime())));
        if (!tempDir.delete())
            throw new IOException("Failed to create temp dir:" + tempDir.getAbsolutePath());
        if (!tempDir.mkdir())
            throw new IOException("Failed to create temp dir:" + tempDir.getAbsolutePath());
        return tempDir;
    }

    private void close(InputStream is) {
        if (is != null)
            try {
                is.close();
            } catch (IOException ignore) {
            }
    }

    public static String surveyMinorVersion(Version version) {
        return version.getMajor() + "." + version.getMinor() + ".x";
    }


}
