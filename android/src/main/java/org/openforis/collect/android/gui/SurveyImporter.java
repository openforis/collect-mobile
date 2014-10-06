package org.openforis.collect.android.gui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.android.util.Unzipper;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.commons.versioning.Version;

import java.io.*;

public class SurveyImporter {
    public static final String DATABASE_NAME = "collect.db";
    public static final String INFO_PROPERTIES_NAME = "info.properties";
    private final String sourceSurveyPath;
    private final Context applicationContext;
    private final String targetSurveyDatabasePath;

    public SurveyImporter(String sourceSurveyPath, Context context, File targetSurveyDatabasePath) {
        this.sourceSurveyPath = sourceSurveyPath;
        this.applicationContext = context;
        this.targetSurveyDatabasePath = targetSurveyDatabasePath.getAbsolutePath();
    }

    public void importSurvey() throws MalformedSurvey, WrongSurveyVersion {
        try {
            File tempDir = unzipSurveyDefinition(sourceSurveyPath);
            String sourceSurveyDatabasePath = new File(tempDir, DATABASE_NAME).getAbsolutePath();
            verifyVersion(tempDir);
            verifyDatabase(sourceSurveyDatabasePath);
            ServiceLocator.recreateNodeDatabase(applicationContext);
            ServiceLocator.deleteModelDatabase(applicationContext);
            applicationContext.openOrCreateDatabase(targetSurveyDatabasePath, 0, null);
            File targetSurveyDatabase = applicationContext.getDatabasePath(targetSurveyDatabasePath);
            FileUtils.copyFile(new File(sourceSurveyDatabasePath), targetSurveyDatabase);
            FileUtils.deleteDirectory(tempDir);
            if (ServiceLocator.surveyService() != null)
                ServiceLocator.surveyService().loadSurvey();
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        } catch (SQLiteException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        }
    }


    public static void importDefaultSurvey(File targetSurveyDatabasePath, Context context) {
        try {
            File tempDir = createTempDir();
            InputStream sourceInput = SurveyImporter.class.getResourceAsStream("/demo.collect-mobile");
            File intermediateSurveyPath = new File(tempDir, "demo.collect-mobile");
            FileOutputStream intermediateOutput = new FileOutputStream(intermediateSurveyPath);
            IOUtils.copy(sourceInput, intermediateOutput);
            new SurveyImporter(intermediateSurveyPath.getAbsolutePath(), context, targetSurveyDatabasePath).importSurvey();
            FileUtils.deleteDirectory(tempDir);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private void verifyVersion(File tempDir) {
        File infoPropertiesFile = new File(tempDir, INFO_PROPERTIES_NAME);
        FileInputStream is = null;
        try {
            is = new FileInputStream(infoPropertiesFile);
            SurveyBackupInfo info = SurveyBackupInfo.parse(is);
            Version version = info.getCollectVersion();
            Version mobileVersion = Collect.getVersion();
            if (differentMinorVersions(version, mobileVersion))
                throw new WrongSurveyVersion(sourceSurveyPath, version);
        } catch (IOException e) {
            throw new MalformedSurvey(sourceSurveyPath, e);
        } finally {
            close(is);
        }

    }

    private boolean differentMinorVersions(Version version1, Version version2) {
        return !surveyMinorVersion(version1).equals(surveyMinorVersion(version2));
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

    /**
     * Reality check on the database file. It needs to be an openable SQLite database file,
     * and contain the ofc_survey table
     */
    private static void verifyDatabase(String surveyDatabasePath) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(surveyDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);
        db.rawQuery("select * from ofc_survey", new String[0]);
    }

    public static String surveyMinorVersion(Version version) {
        return version.getMajor() + "." + version.getMinor() + ".x";
    }

}
