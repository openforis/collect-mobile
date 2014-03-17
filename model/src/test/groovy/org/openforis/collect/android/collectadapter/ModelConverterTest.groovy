package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.viewmodel.UiAttribute
import org.openforis.collect.android.viewmodel.UiInternalNode
import org.openforis.collect.android.viewmodel.UiNode
import org.openforis.collect.android.viewmodel.UiRecord
import org.openforis.collect.android.viewmodel.UiRecordCollection
import org.openforis.collect.model.CollectRecord
import org.openforis.collect.model.CollectSurvey
import org.openforis.collect.model.User
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class ModelConverterTest extends Specification {
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def surveyManager = TestCollectModelFactory.surveyManager
    def recordManager = TestCollectModelFactory.recordManager

    def 'Can convert empty collect survey to view model'() {
        def collectSurvey = importSurvey()

        when:
        def uiSurvey = modelConverter(collectSurvey).toUiSurvey()

        then:
        uiSurvey.label == 'Project label'
    }


    def 'Can convert a CollectRecord to nodes'() {
        def collectSurvey = importSurvey()
        def collectRecord = addRecord('entity-name', collectSurvey)
        def uiSurvey = modelConverter(collectSurvey).toUiSurvey()

        when:
        def uiRecord = modelConverter(collectSurvey).toUiRecord(collectRecord, uiSurvey)

        then:
        uiRecord
        uiRecord.childCount == 1
        def root = uiRecord.firstChild as UiInternalNode
        root.label == 'Tab'
        root.childCount == 1
        def attribute = root.firstChild as UiAttribute
        attribute.label == 'Attribute label'
    }


    def 'Attribute without default value is in state EMPTY'() {
        def collectSurvey = importSurvey()
        def collectRecord = addRecord('entity-name', collectSurvey)
        def uiSurvey = modelConverter(collectSurvey).toUiSurvey()

        when:
        def uiRecord = modelConverter(collectSurvey).toUiRecord(collectRecord, uiSurvey)

        then:
        def root = uiRecord.firstChild as UiInternalNode
        def attribute = root.firstChild as UiAttribute
        attribute.status == UiNode.Status.EMPTY
    }

    // TODO: Fix...
    @Ignore
    def 'Can convert empty UiRecord to collect model'() {
        def collectSurvey = importSurvey()
        def definitionId = collectSurvey.schema.getRootEntityDefinitions().first().id

        def uiRecordCollection = new UiRecordCollection(1, 'entity-name', "Entity label")
        def uiRecord = new UiRecord(2, definitionId, 'entity-name', 'Entity label', uiRecordCollection)

        when:
        def collectRecord = modelConverter(collectSurvey).toCollectRecord(uiRecord, collectSurvey)

        then:
        collectRecord.rootEntity.name == 'entity-name'
    }

    private CollectSurvey importSurvey() {
        surveyManager.importModel(idm, 'survey', false)
    }

    private CollectRecord addRecord(String entityName, CollectSurvey collectSurvey) {
        recordManager.create(collectSurvey, entityName, new User(), null)
    }

    private ModelConverter modelConverter(CollectSurvey collectSurvey) {
        new ModelConverter(collectSurvey, new Definitions(collectSurvey))
    }

    private InputStream getIdm() {
        idmXmlStream {
            project('Project label')
            schema {
                entity('entity-name', 'Entity label') {
                    text('attribute-name', 'Attribute label')
                }
            }
        }
    }


}
