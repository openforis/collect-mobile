package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.viewmodel.*
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository
import org.openforis.collect.android.viewmodelmanager.NodeTestDatabase
import org.openforis.collect.android.viewmodelmanager.ViewModelManager
import spock.lang.Specification

import static org.openforis.collect.android.viewmodel.UiNode.Status.EMPTY
import static org.openforis.collect.android.viewmodelmanager.ViewModelRepository.DatabaseViewModelRepository

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class CollectModelBackedSurveyServiceTest extends Specification {
    public static final File UNUSED_EXPORT_FILE = null
    def database = new NodeTestDatabase()
    def collectModelManager = TestCollectModelFactory.collectModelManager(database)
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def surveyService = new CollectModelBackedSurveyService(
            new ViewModelManager(
                    new DatabaseViewModelRepository(
                            collectModelManager,
                            new DataSourceNodeRepository(database)
                    )
            ),
            collectModelManager, UNUSED_EXPORT_FILE
    )

    def cleanup() {
        database.reset()
    }

    def 'Can import survey from IDM XML stream'() {
        when:
        def uiSurvey = surveyService.importSurvey(idm)

        then:
        uiSurvey.label == 'Project label'
        uiSurvey.childCount == 1
        uiSurvey.firstChild instanceof UiRecordCollection
    }

    def 'Can load previously imported survey'() {
        def importedUiSurvey = surveyService.importSurvey(idm)

        when:
        def loadedUiSurvey = surveyService.loadSurvey(importedUiSurvey.name)

        then:
        loadedUiSurvey.label == 'Project label'
        loadedUiSurvey.childCount == 1
        loadedUiSurvey.firstChild instanceof UiRecordCollection
    }


    def 'Loading a never imported survey returns null'() {
        expect:
        surveyService.loadSurvey('never-imported') == null
    }

    def 'Can add record'() {
        def uiSurvey = surveyService.importSurvey(idm)

        when:
        def uiRecord = surveyService.addRecord('entity-name')

        then:
        uiSurvey.childCount == 1
        uiSurvey.firstChild instanceof UiRecordCollection
        uiSurvey.firstChild.label == 'Entity label'

        uiRecord.label == 'Entity label'
        uiRecord.childCount == 1
        def tab = uiRecord.firstChild as UiInternalNode
        tab.label == 'Tab'
        tab.childCount == 3
        tab.getChildAt(0) instanceof UiTextAttribute
        tab.getChildAt(1) instanceof UiEntityCollection
        tab.getChildAt(2) instanceof UiAttributeCollection
    }

    def 'Can add entity'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        UiEntityCollection uiEntityCollection = findUiEntityCollection('multiple-entity-name', uiRecord)
        surveyService.selectNode(uiEntityCollection.id)

        when:
        def uiEntity = surveyService.addEntity()

        then:
        uiEntityCollection.childCount == 1
        uiEntityCollection.firstChild.is uiEntity

        uiEntity.name == 'multiple-entity-name'
        uiEntity.childCount == 2
        uiEntity.getChildAt(0) instanceof UiTextAttribute
        uiEntity.getChildAt(1) instanceof UiEntityCollection
    }

    def 'When adding entity, status of entity and descendant nodes are updated'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        UiEntityCollection uiEntityCollection = findUiEntityCollection('multiple-entity-name', uiRecord)
        surveyService.selectNode(uiEntityCollection.id)

        when:
        def uiEntity = surveyService.addEntity()

        then:
        uiEntity.status == EMPTY
        uiEntity.firstChild.status == EMPTY
    }

    def 'Can add deeply nested entity'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        UiEntityCollection uiEntityCollection = findUiEntityCollection('multiple-entity-name', uiRecord)
        surveyService.selectNode(uiEntityCollection.id)
        def uiEntity = surveyService.addEntity()
        UiEntityCollection nestedEntityCollection = findUiEntityCollection('deeply-nested-entity-name', uiEntity)
        surveyService.selectNode(nestedEntityCollection.id)

        when:
        def nestedEntity = surveyService.addEntity()

        then:
        nestedEntityCollection.childCount == 1
        nestedEntityCollection.firstChild.is nestedEntity

        nestedEntity.name == 'deeply-nested-entity-name'
        nestedEntity.childCount == 1
        nestedEntity.firstChild instanceof UiTextAttribute
    }


    def 'Can update an attribute'() {
        def importedSurvey = surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def attribute = findUiTextAttribute('uiAttribute-name', uiRecord)
        attribute.text = 'Updated text'

        when:
        surveyService.updateAttribute(attribute)

        then:
        surveyService.loadSurvey(importedSurvey.name)
        def loadedRecord = surveyService.selectRecord(uiRecord.getId())
        def loadedAttribute = findUiTextAttribute('uiAttribute-name', loadedRecord)
        loadedAttribute.text == 'Updated text'
    }

    def 'When setting the value of an empty attribute, it changes state from EMPTY to OK'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def attribute = findUiTextAttribute('uiAttribute-name', uiRecord)
        attribute.text = 'non-empty value'
        assert attribute.status == EMPTY

        when:
        surveyService.updateAttribute(attribute)

        then:
        attribute.status == UiNode.Status.OK
    }


    def 'When adding a record, status of record and descendant nodes are updated'() {
        surveyService.importSurvey(idm)

        when:
        def uiRecord = surveyService.addRecord('entity-name')

        then:
        def attribute = findUiTextAttribute('uiAttribute-name', uiRecord)
        attribute.status == EMPTY
        attribute.getParent().status == EMPTY
        uiRecord.status == EMPTY
    }

    def 'When selecting a record, status of record nodes is up-to-date'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')

        when:
        def selectedRecord = surveyService.selectRecord(uiRecord.id)

        then:
        def attribute = findUiTextAttribute('uiAttribute-name', selectedRecord)
        attribute.status == EMPTY
        attribute.getParent().status == EMPTY
        selectedRecord.status == EMPTY
    }

    def 'Can lookup node'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def rootEntity = uiRecord.getFirstChild()

        when:
        def lookedUpRootEntity = surveyService.lookupNode(rootEntity.getId())

        then:
        lookedUpRootEntity.is rootEntity
    }


    def 'Can get key attributes from record'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def expectedKeyAttribute = findUiTextAttribute('uiAttribute-name', uiRecord)

        expect:
        uiRecord.keyAttributes == [expectedKeyAttribute]
    }


    def 'Record placeholders contains attributes keys'() {
        def importedSurvey = surveyService.importSurvey(idm)
        surveyService.addRecord('entity-name')

        when:
        def loadedSurvey = surveyService.loadSurvey(importedSurvey.name)

        then:
        def recordPlaceholder = loadedSurvey.firstChild.firstChild
        recordPlaceholder.getKeyAttributes().size() == 1
    }

    def 'Record placeholder key attributes are updated when actual attributes are updated'() {
        def importedSurvey = surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def recordPlaceholder = importedSurvey.firstChild.firstChild as UiRecord.Placeholder
        assert recordPlaceholder.keyAttributes.first().text == null
        def attribute = findUiTextAttribute('uiAttribute-name', uiRecord)
        attribute.text = 'Updated text'

        when:
        surveyService.updateAttribute(attribute)

        then:
        recordPlaceholder.keyAttributes.first().text == 'Updated text'
    }


    def 'Can add attribute to attribute collection'() {
        surveyService.importSurvey(idm)
        def uiRecord = surveyService.addRecord('entity-name')
        def attributeCollection = findNode(uiRecord) {
            it.name == 'uiAttributeCollection-name'
        }
        surveyService.selectNode(attributeCollection.id)

        when:
        def attribute = surveyService.addAttribute()

        then:
        attribute.definition.id.isNumber()
    }


    private UiEntityCollection findUiEntityCollection(name, UiNode uiNode) {
        findNode(uiNode) {
            it.name == name && it instanceof UiEntityCollection
        } as UiEntityCollection
    }

    private UiTextAttribute findUiTextAttribute(name, UiNode uiNode) {
        findNode(uiNode) {
            it.name == name && it instanceof UiTextAttribute
        } as UiTextAttribute
    }

    private InputStream getIdm() {
        idmXmlStream {
            project('Project label')
            schema {
                entity('entity-name', 'Entity label') {
                    text('uiAttribute-name', 'Attribute label', [key: true])
                    entity('multiple-entity-name', 'Multiple entity label', [multiple: true]) {
                        text('uiAttribute-name2', 'Attribute label2')
                        entity('deeply-nested-entity-name', 'Deeply nested entity label', [multiple: true]) {
                            text('uiAttribute-name3', 'Attribute label3')
                        }
                    }
                    text('uiAttributeCollection-name', 'Attribute Collection label', [multiple: true])
                }
            }
        }
    }

    private UiNode findNode(UiNode node, Closure constraint) {
        if (constraint(node))
            return node
        else if (node instanceof UiInternalNode)
            return node.children.collect { findNode(it, constraint) }.find()
        return null
    }
}
