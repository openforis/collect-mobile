package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.viewmodel.UiTaxon
import org.openforis.collect.model.CollectSurvey
import spock.lang.Specification

import static org.openforis.collect.android.IdGenerator.nextId

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class TaxonRepositoryTest extends Specification {
    @Delegate IdmBuilder builder = new IdmBuilder()
    def database = new ModelTestDatabase()
    TaxonRepository repo
    Taxonomy taxonomy
    Taxonomy anotherTaxonomy

    def setup() {
        int surveyId = insertSurvey()
        taxonomy = insertTaxonomy(surveyId, 'taxonomy')
        anotherTaxonomy = insertTaxonomy(surveyId, 'another-taxonomy')
        repo = new TaxonRepository(database)
    }

    def cleanup() {
        database.reset()
    }

    def 'No matches returns empty list'() {
        insertTaxon('code', 'name', taxonomy)

        expect: repo.find('notExpectedToBeFound', taxonomy.name, 10).empty
    }

    def 'Can match a code and scientific name'() {
        insertTaxon('code', 'name', taxonomy)
        insertTaxon('another-code', 'another-name', taxonomy) // Not expected to match - query not in code or name
        insertTaxon('code', 'name', anotherTaxonomy) // Not expected to match - wrong taxonomy

        expect:
            repo.find("c", taxonomy.name, 10)*.code == ['code']
            repo.find("n", taxonomy.name, 10)*.code == ['code']
            repo.find("C", taxonomy.name, 10)*.code == ['code']
            repo.find("N", taxonomy.name, 10)*.code == ['code']
    }

    def 'Can limit results'() {
        insertTaxon('a1', 'name1', taxonomy)
        insertTaxon('a2', 'name2', taxonomy)
        insertTaxon('a3', 'name3', taxonomy)

        expect:
            repo.find("a", taxonomy.name, 2).size() == 2
    }

    private void insertTaxon(String code, String scientificName, Taxonomy taxonomy) {
        def taxon = new UiTaxon(code, scientificName)
        database.sql.executeInsert("""
            INSERT INTO ofc_taxon(id, taxon_id, code, scientific_name, taxon_rank, taxonomy_id, step, parent_id)
            VALUES(${nextId()}, ${nextId()}, $taxon.code, $taxon.scientificName, 'species', $taxonomy.id, 9, NULL)
        """)
    }

    private Taxonomy insertTaxonomy(int surveyId, String taxonomy) {
        def taxonomyId = nextId()
        database.sql.executeInsert("""
            INSERT INTO ofc_taxonomy(id, survey_id, survey_work_id, name, metadata)
            VALUES($taxonomyId, $surveyId, NULL, $taxonomy, 'metadata')
        """)
        return new Taxonomy(id: taxonomyId, name: taxonomy)
    }

    private int insertSurvey() {
        def surveyId = nextId()
        database.sql.executeInsert("""
            INSERT INTO ofc_survey(id, name, uri, idml, date_created, date_modified)
            VALUES ($surveyId, 'survey', 'http://the/uri', '<idm/>', ${new Date()}, ${new Date()})
        """)
        surveyId
    }


    CollectSurvey survey() {
        def idm = idmXmlStream {
            schema {
                entity('root', 'Root') {
                }
            }
        }

        def manager = TestCollectModelFactory.surveyManager
        return manager.importModel(idm, "survey", false)
    }

    static class Taxonomy {
        int id
        String name
    }

}
