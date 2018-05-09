package org.openforis.collect.android.sqlite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.util.persistence.PersistenceException;
import org.openforis.collect.android.util.persistence.SchemaChange;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

/**
 * @author Daniel Wiell
 */
public class AndroidDatabase implements Database {
    public static final String ACTION_PREPARE_EJECT = "org.openforis.collect.android.sqlite.Unmount";
    private final OpenHelper openHelper;

    private DataSource dataSource;

    public AndroidDatabase(Context context, File databasePath) {
        this(new NodeSchemaChangeLog(Collections.<SchemaChange>emptyList()), context, databasePath);
    }

    public AndroidDatabase(NodeSchemaChangeLog schemaChangeLog, Context context, File databasePath) {
        dataSource = new AndroidDataSource(databasePath);
        openHelper = new OpenHelper(schemaChangeLog, context.getApplicationContext(), databasePath);
        listenToPrepareEjectionBroadcasts(context);
        listenToStorageEjectionBroadcasts(context);
        setupDatabase(databasePath);
        AndroidFiles.makeDiscoverable(databasePath, context);
        SQLiteDatabase db = openOrCreateDatabase();
        schemaChangeLog.apply(db);
        db.close();
    }

    public void close() {
        openHelper.close();
        ((AndroidDataSource) dataSource).close();
    }

    private void listenToStorageEjectionBroadcasts(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("file");
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.i("android_database", "Received storage ejection event for " + dataSource);
                openHelper.close();
                ((AndroidDataSource) dataSource).close();
            }
        }, filter);
    }

    private void listenToPrepareEjectionBroadcasts(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PREPARE_EJECT);
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.i("android_database", "Received storage ejection request for " + dataSource);
                close();
            }
        }, filter);
    }

    private void setupDatabase(File databasePath) {
        if (!databasePath.getParentFile().exists()) {
            if (!databasePath.getParentFile().mkdirs())
                throw new IllegalStateException("Failed to create database directory: " + databasePath.getParentFile());
        }
        SQLiteDatabase database = openOrCreateDatabase();
        database.close();
    }


    public DataSource dataSource() {
        return dataSource;
    }

    public synchronized <T> T execute(AndroidDatabaseCallback<T> AndroidDatabaseCallback) {
        SQLiteDatabase database = null;
        try {
            database = openOrCreateDatabase();
            return AndroidDatabaseCallback.execute(database);
        } finally {
            close(database);
        }
    }

    public synchronized <T> T execute(ConnectionCallback<T> connectionCallback) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            T result = connectionCallback.execute(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new PersistenceException(e);
        } finally {
            try {
                if (connection != null)
                    connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
            close(connection);
        }
    }

    private void rollback(Connection connection) {
        if (connection != null)
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
    }

    private SQLiteDatabase openOrCreateDatabase() {
        return openHelper.getWritableDatabase();
    }

    private void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Exception when closing connection", e);
        }
    }

    private void close(SQLiteDatabase database) {
        if (database != null && database.isOpen())
            database.close();
    }

    public static class OpenHelper extends SQLiteOpenHelper {
        private final NodeSchemaChangeLog schemaChangeLog;

        private OpenHelper(NodeSchemaChangeLog schemaChangeLog, Context context, File databasePath) {
            super(context, databasePath.getAbsolutePath(), null, schemaChangeLog.getVersion());
            this.schemaChangeLog = schemaChangeLog;
        }

        public void onCreate(SQLiteDatabase db) {
            schemaChangeLog.init(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            schemaChangeLog.apply(db, oldVersion, newVersion);
        }
    }
}
