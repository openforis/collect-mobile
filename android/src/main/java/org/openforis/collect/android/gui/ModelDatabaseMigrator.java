package org.openforis.collect.android.gui;

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import liquibase.database.DatabaseConnection;
import liquibase.database.core.SQLiteDatabase;
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
        File surveyDir = new File(AppDirs.surveysDir(context), surveyName); // TODO: Use the survey dir instead
        File collectVersionFile = new File(surveyDir, "collect-version.properties");

        migrateIfNeeded(currentVersion, collectVersionFile);

        storeCollectVersion(currentVersion, collectVersionFile);
    }

    private void migrateIfNeeded(Version currentVersion, File collectVersionFile) {
        Properties collectVersion = loadVersionFromFile(collectVersionFile);
        if (collectVersion == null) {
            migrate();
        } else {
            int majorVersion = getIntProperty(collectVersion, "major");
            int minorVersion = getIntProperty(collectVersion, "minor");

            if (majorVersion < currentVersion.getMajor() || minorVersion < currentVersion.getMinor())
                migrate();
        }
    }

    private int getIntProperty(Properties props, String propName) {
        String value = props.getProperty(propName);
        return StringUtils.isBlank(value) ? 0 : Integer.parseInt(value);
    }

    public void migrate() {
        long start = System.currentTimeMillis();
        new ModelDatabaseSchemaUpdater().update(database, new SQLiteDatabase() {
            public static final String PRODUCT_NAME = "SQLite for Android";

            @Override
            public boolean isLocalDatabase() throws DatabaseException {
                return true;
            }

            public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
                    throws DatabaseException {
                return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println(time);
    }

    private void storeCollectVersion(Version currentVersion, File collectVersionFile) {
        try {
            Properties collectVersion = new Properties();
            collectVersion.setProperty("major", String.valueOf(currentVersion.getMajor()));
            collectVersion.setProperty("minor", String.valueOf(currentVersion.getMinor()));
            collectVersion.store(new FileOutputStream(collectVersionFile), "");
            AndroidFiles.makeDiscoverable(collectVersionFile, context);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to store collect-version.properties file", e);
        }
    }

    private static Properties loadVersionFromFile(File file) {
        if (file.exists()) {
            FileInputStream in = null;
            try {
                Properties collectVersion = new Properties();
                in = new FileInputStream(file);
                collectVersion.load(in);
                return collectVersion;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to determine collect version", e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return null;
    }
}
