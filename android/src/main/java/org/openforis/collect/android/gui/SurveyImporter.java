package org.openforis.collect.android.gui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.util.Unzipper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SurveyImporter {
    public static final String DATABASE_NAME = "collect.db";
    private final String sourceSurveyPath;
    private final Context applicationContext;
    private final String targetSurveyDatabasePath;

    public SurveyImporter(String sourceSurveyPath, Context applicationContext, File targetSurveyDatabasePath) {
        this.sourceSurveyPath = sourceSurveyPath;
        this.applicationContext = applicationContext;
        this.targetSurveyDatabasePath = targetSurveyDatabasePath.getAbsolutePath();
    }

    public void importSurvey() {
        try {
            File tempDir = unzipSurveyDatabase(sourceSurveyPath);
            String sourceSurveyDatabasePath = new File(tempDir, DATABASE_NAME).getAbsolutePath();
            verifyDatabase(sourceSurveyDatabasePath);
            applicationContext.openOrCreateDatabase(targetSurveyDatabasePath, 0, null);
            File targetSurveyDatabase = applicationContext.getDatabasePath(targetSurveyDatabasePath);
            FileUtils.copyFile(new File(sourceSurveyDatabasePath), targetSurveyDatabase);
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            notifyAboutImportFailure(e);
        } catch (SQLiteException e) {
            notifyAboutImportFailure(e);
        }
    }

    private void notifyAboutImportFailure(Exception e) {
        String message = applicationContext.getResources().getString(R.string.toast_import_survey_failed, sourceSurveyPath);
        Log.w(ServiceLocator.class.getSimpleName(), message, e);
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
    }

    private static File unzipSurveyDatabase(String surveyDatabasePath) throws IOException {
        File folder = createTempDir();
        File zipFile = new File(surveyDatabasePath);
        if (!zipFile.exists())
            throw new FileNotFoundException("File not found: " + surveyDatabasePath);
        new Unzipper(zipFile, folder).unzip(DATABASE_NAME);

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

    /**
     * Reality check on the database file. It needs to be an openable SQLite database file,
     * and contain the ofc_survey table
     */
    private static void verifyDatabase(String surveyDatabasePath) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(surveyDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);
        db.rawQuery("select * from ofc_survey", new String[0]);
    }

}
