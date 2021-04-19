package org.openforis.collect.android.collectadapter

import groovy.sql.Sql
import liquibase.database.AbstractDatabase
import liquibase.database.core.H2Database
import liquibase.database.core.PostgresDatabase
import org.h2.jdbc.JdbcConnection
import org.h2.jdbc.JdbcDatabaseMetaData
import org.h2.jdbcx.JdbcDataSource
import org.openforis.collect.android.databaseschema.ModelDatabaseSchemaUpdater
import org.openforis.collect.android.util.persistence.ConnectionCallback
import org.openforis.collect.android.util.persistence.Database

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.SQLException

/**
 * @author Daniel Wiell
 */
class ModelTestDatabase implements Database {
    private static final String RESET_SCRIPT = '''
        delete from ofc_code_list;
        delete from ofc_record;
        delete from ofc_sampling_design;
        delete from ofc_taxon_vernacular_name;
        delete from ofc_taxon;
        delete from ofc_taxonomy;
        delete from ofc_user_role;
        delete from ofc_user;
        delete from ofc_logo;
        delete from ofc_survey;
        delete from ofc_survey_work;
        delete from ofc_config;
        delete from ofc_application_info;
        '''
    private static final String INIT_URL = 'jdbc:h2:mem:model;MODE=PostgreSQL;REFERENTIAL_INTEGRITY=false;DB_CLOSE_DELAY=-1'
    private static final String URL = 'jdbc:h2:mem:model;SCHEMA=collect;MODE=PostgreSQL;REFERENTIAL_INTEGRITY=false;DB_CLOSE_DELAY=-1'

    private static boolean initialized
    private static DataSource dataSource

    ModelTestDatabase() {
        initDatabase()
    }

    private void initDatabase() {
        if (!initialized) {
            initialized = true
            setupSchema()
        } else
            reset()
    }

    private void setupSchema() {
        new Sql(new JdbcDataSource(url: INIT_URL, user: 'sa', password: 'sa')).execute('''
            CREATE SCHEMA collect;
            SET SCHEMA collect;
            CREATE ALIAS IF NOT EXISTS setval FOR "org.openforis.collect.android.collectadapter.DatabaseFunctions.setSequenceValue";
            COMMIT;
        ''')
        dataSource = new JdbcDataSource(url: URL, user: 'sa', password: 'sa')
        new ModelDatabaseSchemaUpdater().update(this, new H2DatabaseInPostgresMode())
    }

    void reset() {
        new Sql(dataSource).execute(RESET_SCRIPT)
    }

    DataSource dataSource() {
        return dataSource
    }

    Sql getSql() {
        def sql = new Sql(new JdbcDataSource(url: 'jdbc:h2:mem:model;SCHEMA=collect;MODE=PostgreSQL;REFERENTIAL_INTEGRITY=false;DB_CLOSE_DELAY=-1', user: 'sa', password: 'sa'))
        return sql
    }

    def <T> T execute(ConnectionCallback<T> connectionCallback) {
        T result = null
        new Sql(dataSource).withTransaction {
            result = connectionCallback.execute(it)
        }
        return result
    }

    private static class H2DatabaseInPostgresMode extends AbstractDatabase {
        @Delegate
        private final H2Database delegate = new H2Database();
        private final PostgresDatabase postgresDatabase = new PostgresDatabase();

        H2DatabaseInPostgresMode() {
            //super.unquotedObjectsAreUppercased = delegate.unquotedObjectsAreUppercased
            super.currentDateTimeFunction = delegate.currentDateTimeFunction
            //delegate.getDateFunctions().each { this.dateFunctions.add(it) }
            //super.sequenceNextValueFunction = delegate.sequenceNextValueFunction;
            //super.sequenceCurrentValueFunction = delegate.sequenceCurrentValueFunction;
        }

        String getShortName() {
            return postgresDatabase.getShortName()
        }

        protected String getDefaultDatabaseProductName() {
            return postgresDatabase.getDefaultDatabaseProductName()
        }

        String getDefaultSchemaName() {
            return 'collect'
        }

        protected String getConnectionSchemaName() {
            return 'collect'
        }
    }
}
