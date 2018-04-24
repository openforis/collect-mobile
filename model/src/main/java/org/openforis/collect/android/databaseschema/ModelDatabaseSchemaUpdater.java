package org.openforis.collect.android.databaseschema;

import org.openforis.collect.android.util.persistence.Database;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * @author Daniel Wiell
 */
public class ModelDatabaseSchemaUpdater {
    private static final String LIQUIBASE_CHANGE_LOG = "org/openforis/collect/db/changelog/db.changelog-master.xml";

    public void update(Database db, final liquibase.database.Database liquibaseDatabase) {
        try {
            liquibaseDatabase.setConnection(new JdbcConnection(db.dataSource().getConnection()));
            Liquibase liquibase = new Liquibase(LIQUIBASE_CHANGE_LOG,
                    new ClassLoaderResourceAccessor(), liquibaseDatabase);
            liquibase.update((String) null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to setup database schema", e);
        } finally {
            close(liquibaseDatabase);
        }
    }

    private void close(liquibase.database.Database database) {
        try {
            if (database != null)
                database.close();
        } catch (DatabaseException e) {
            throw new IllegalStateException("Failed to close liquibase database", e);
        }
    }
}
