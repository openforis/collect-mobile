package org.openforis.collect.android.gui;

import android.content.Context;

import org.openforis.collect.Collect;
import org.openforis.collect.android.databaseschema.ModelDatabaseSchemaUpdater;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.commons.versioning.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.database.core.AndroidSQLiteDatabase;
import liquibase.exception.DatabaseException;

public class ModelDatabaseMigrator {
    private static final Logger LOG = Logger.getLogger(ModelDatabaseMigrator.class.getName());

    private final Database database;
    private final String surveyName;
    private final Context context;

    public ModelDatabaseMigrator(Database database, String surveyName, Context context) {
        this.database = database;
        this.surveyName = surveyName;
        this.context = context;
    }

    public void migrateIfNeeded() {
        Version currentVersion = Collect.VERSION;
        Properties collectVersion = new Properties();
        File surveyDir = new File(AppDirs.surveysDir(context), surveyName); // TODO: Use the survey dir instead
        File collectVersionFile = new File(surveyDir, "collect-version.properties");
        if (collectVersionFile.exists())
            migrateIfNeeded(currentVersion, collectVersion, collectVersionFile);
        else
            migrate();

        try {
            collectVersion.setProperty("major", String.valueOf(currentVersion.getMajor()));
            collectVersion.setProperty("minor", String.valueOf(currentVersion.getMinor()));
            collectVersion.store(new FileOutputStream(collectVersionFile), "");
            AndroidFiles.makeDiscoverable(collectVersionFile, context);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to store collect-version.properties file", e);
        }
    }

    private void migrateIfNeeded(Version currentVersion, Properties collectVersion, File collectVersionFile) {
        try {
            FileInputStream in = new FileInputStream(collectVersionFile);
            collectVersion.load(in);

            int majorVersion = Integer.parseInt(collectVersion.getProperty("major"));
            int minorVersion = Integer.parseInt(collectVersion.getProperty("minor"));

            if (majorVersion < currentVersion.getMajor() || minorVersion < currentVersion.getMinor())
                migrate();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to check if migration is needed", e);
            migrate();
        }
    }

    public void migrate() {
        long start = System.currentTimeMillis();
        new ModelDatabaseSchemaUpdater().update(database, new AndroidSQLiteDatabase() {
            public boolean isLocalDatabase() throws DatabaseException {
                return true;
            }
            public void rollback() throws DatabaseException {
                super.rollback();
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println(time);
    }
}
