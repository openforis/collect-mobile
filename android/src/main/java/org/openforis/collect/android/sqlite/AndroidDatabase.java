package org.openforis.collect.android.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
    private final OpenHelper openHelper;

    private DataSource dataSource;

    public AndroidDatabase(Context context, String databaseName) {
        this(new NodeSchemaChangeLog(Collections.<SchemaChange>emptyList()), context, databaseName);
    }

    public AndroidDatabase(NodeSchemaChangeLog schemaChangeLog, Context context, String databaseName) {
        File databasePath = context.getDatabasePath(databaseName);
        dataSource = new AndroidDataSource(databasePath);
        openHelper = new OpenHelper(schemaChangeLog, context.getApplicationContext(), databaseName);
        setupDatabase(databasePath);
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
//        if (database != null && database.isOpen())
//            database.close();
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        private final NodeSchemaChangeLog schemaChangeLog;

        private OpenHelper(NodeSchemaChangeLog schemaChangeLog, Context context, String name) {
            super(context, name, null, schemaChangeLog.getVersion());
            this.schemaChangeLog = schemaChangeLog;
        }

        public void onCreate(SQLiteDatabase db) {
            schemaChangeLog.apply(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            schemaChangeLog.apply(db, oldVersion, newVersion);
        }
    }
}

