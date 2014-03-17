package org.openforis.collect.android.viewmodelmanager

import groovy.sql.Sql
import org.h2.jdbcx.JdbcDataSource
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog
import org.openforis.collect.android.util.persistence.ConnectionCallback
import org.openforis.collect.android.util.persistence.Database
import org.openforis.collect.android.util.persistence.SchemaChange

import javax.sql.DataSource

/**
 * @author Daniel Wiell
 */
class NodeTestDatabase implements Database {
    private static final String RESET_SCRIPT = "delete from ofc_view_model;"
    private static final String URL = "jdbc:h2:mem:nodes;DB_CLOSE_DELAY=-1"

    private static boolean initialized
    private static DataSource dataSource

    NodeTestDatabase() {
        initDatabase()
    }

    private void initDatabase() {
        if (!initialized) {
            initialized = true
            dataSource = new JdbcDataSource(url: URL, user: 'sa', password: 'sa')
            setupSchema()
        } else reset()
    }

    private void setupSchema() {
        def sql = new Sql(dataSource)
        sql.withTransaction {
            for (SchemaChange change : new NodeDatabaseSchemaChangeLog().changes())
                for (String statement : change.statements())
                    sql.execute(statement)
        }
    }

    void reset() {
        new Sql(dataSource).execute(RESET_SCRIPT)
    }

    DataSource dataSource() {
        return dataSource
    }

    def <T> T execute(ConnectionCallback<T> connectionCallback) {
        T result = null
        new Sql(dataSource).withTransaction {
            result = connectionCallback.execute(it)
        }
        return result
    }
}

