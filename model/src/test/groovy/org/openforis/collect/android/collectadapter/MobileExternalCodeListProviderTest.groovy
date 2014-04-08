package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.IdGenerator
import org.openforis.collect.model.CollectSurvey
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class MobileExternalCodeListProviderTest extends Specification {
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def database = new TestDatabase()
    def survey = survey()
    def provider = new MobileExternalCodeListProvider(database)

    def cleanup() {
        dropTable()
    }

    def 'Can get root items'() {
        createExternalTable([level1: 'first'],
                [level1: 'second'],
                [level1: 'first', level2: 'nested'])

        when:
        def items = provider.getRootItems(survey.getCodeList('external_table'))

        then:
        items.collect { it.code } == ['first', 'second']
    }

    def 'Can get child items'() {
        createExternalTable([level1: 'first'],
                [level1: 'second'],
                [level1: 'first', level2: 'first_nested'],
                [level1: 'first', level2: 'second_nested'],
                [level1: 'first', level2: 'first_nested', level3: 'deeply_nested']
        )
        def rootItems = provider.getRootItems(survey.getCodeList('external_table'))
        def parent = rootItems.first()

        when:
        def items = provider.getChildItems(parent)

        then:
        items.collect { it.code } == ['first_nested', 'second_nested']
    }

    def dropTable() {
        database.sql.execute("DROP TABLE IF EXISTS external_table" as String)
    }

    void createExternalTable(Map... rows) {
        database.sql.execute("""
                CREATE TABLE IF NOT EXISTS external_table(
                    id INTEGER PRIMARY KEY NOT NULL,
                    survey_id INTEGER,
                    survey_work_id INTEGER,
                    level1 TEXT NOT NULL,
                    level2 TEXT,
                    level3 TEXT,
                    location TEXT NOT NULL
                )
        """.toString())
        rows.each { insert(it) }
    }

    void insert(Map item) {
        database.sql.executeInsert("""
            INSERT INTO external_table(id, survey_id, survey_work_id, level1, level2, level3, location)
            VALUES(${IdGenerator.nextId()}, $survey.id, null, $item.level1, $item.level2, $item.level3, 'Unspecified')""")
    }

    CollectSurvey survey() {
        def idm = idmXmlStream {
            codeLists {
                list(id: 123, lookup: 'external_table', name: 'external_table') {
                    label(type: 'item', 'Sample Ids')
                    codingScheme(scope: 'local')
                    hierarchy {
                        level(name: 'level1') {
                            label('Cluster id')
                        }
                        level(name: 'level2') {
                            label('Plot id')
                        }
                        level(name: 'level3') {
                            label('Subplot id')
                        }
                    }
                }
            }
            schema {
                entity('foo', 'Bar') {
                    text('uiAttribute name', 'The label')
                }
            }
        }
        return TestCollectModelFactory.surveyManager.importModel(idm, "survey", false)
    }

}