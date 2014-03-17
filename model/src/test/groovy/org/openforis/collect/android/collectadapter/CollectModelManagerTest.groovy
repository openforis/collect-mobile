package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.util.persistence.Database
import spock.lang.Specification

import javax.sql.DataSource

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class CollectModelManagerTest extends Specification {
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def database = Mock(Database)
    def manager

    def setup() {
        database.dataSource() >> Mock(DataSource)
        manager = TestCollectModelFactory.collectModelManager(database)
    }

    def 'Can import survey'() {
        when:
        def collectSurvey = manager.importSurvey idmXmlStream {
            schema {
                entity('foo', 'Bar') {
                    text('attribute name', 'The label')
                }
            }
        }

        then:
        collectSurvey

    }
}