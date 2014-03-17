package org.openforis.collect.android.collectadapter

import groovy.sql.Sql
import org.h2.jdbcx.JdbcDataSource
import org.openforis.collect.android.util.persistence.ConnectionCallback
import org.openforis.collect.android.util.persistence.Database

import javax.sql.DataSource

/**
 * @author Daniel Wiell
 */
class TestDatabase implements Database {
    private final DataSource dataSource = new JdbcDataSource(
            url: "jdbc:h2:mem:${Math.random()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            user: 'sa', password: 'sa')

    DataSource dataSource() {
        return dataSource
    }

    Sql getSql() {
        new Sql(dataSource)
    }

    def <T> T execute(ConnectionCallback<T> connectionCallback) {
        T result = null
        new Sql(dataSource).withTransaction {
            result = connectionCallback.execute(it)
        }
        return result
    }
}
