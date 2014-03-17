package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.viewmodel.*
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository
import org.openforis.collect.android.viewmodelmanager.NodeTestDatabase
import org.openforis.collect.android.viewmodelmanager.ViewModelManager
import org.openforis.collect.android.viewmodelmanager.ViewModelRepository
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class CollectModelBackedSurveyServiceTest extends Specification {
    def database = new NodeTestDatabase()
    def collectModelManager = TestCollectModelFactory.collectModelManager(database)
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def survey = new CollectModelBackedSurveyService(
            new ViewModelManager(
                    new ViewModelRepository(
                            collectModelManager,
                            new DataSourceNodeRepository(database)
                    )
            ),
            collectModelManager
    )

    def cleanup() {
        database.reset()
    }

    def 'Can import survey from IDM XML stream'() {
        when:
        def uiSurvey = survey.importSurvey(idm)

        then:
        uiSurvey.label == 'Project label'
        uiSurvey.childCount == 1
        uiSurvey.firstChild instanceof UiRecordCollection
    }

    def 'Can load previously imported survey'() {
        def importedUiSurvey = survey.importSurvey(idm)

        when:
        def loadedUiSurvey = survey.loadSurvey(importedUiSurvey.name)

        then:
        loadedUiSurvey.label == 'Project label'
        loadedUiSurvey.childCount == 1
        loadedUiSurvey.firstChild instanceof UiRecordCollection
    }


    def 'Loading a never imported survey returns null'() {
        expect: survey.loadSurvey('never-imported') == null
    }

    def 'Can add record'() {
        def uiSurvey = survey.importSurvey(idm)

        when:
        def uiRecord = survey.addRecord('entity-name')

        then:
        uiSurvey.childCount == 1
        uiSurvey.firstChild instanceof UiRecordCollection
        uiSurvey.firstChild.label == 'Entity label'

        uiRecord.label == 'Entity label'
        uiRecord.childCount == 1
        def tab = uiRecord.firstChild as UiInternalNode
        tab.label == 'Tab'
        tab.childCount == 2
        tab.getChildAt(0) instanceof UiTextAttribute
        tab.getChildAt(1) instanceof UiEntityCollection
    }

    def 'Can add entity'() {
        survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        UiEntityCollection uiEntityCollection = findUiEntityCollection('multiple-entity-name', uiRecord)
        survey.selectNode(uiEntityCollection.id)

        when:
        def uiEntity = survey.addEntity()

        then:
        uiEntityCollection.childCount == 1
        uiEntityCollection.firstChild.is uiEntity

        uiEntity.name == 'multiple-entity-name'
        uiEntity.childCount == 2
        uiEntity.getChildAt(0) instanceof UiTextAttribute
        uiEntity.getChildAt(1) instanceof UiEntityCollection
    }


    def 'Can add deeply nested entity'() {
        survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        UiEntityCollection uiEntityCollection = findUiEntityCollection('multiple-entity-name', uiRecord)
        survey.selectNode(uiEntityCollection.id)
        def uiEntity = survey.addEntity()
        UiEntityCollection nestedEntityCollection = findUiEntityCollection('deeply-nested-entity-name', uiEntity)
        survey.selectNode(nestedEntityCollection.id)

        when:
        def nestedEntity = survey.addEntity()

        then:
        nestedEntityCollection.childCount == 1
        nestedEntityCollection.firstChild.is nestedEntity

        nestedEntity.name == 'deeply-nested-entity-name'
        nestedEntity.childCount == 1
        nestedEntity.firstChild instanceof UiTextAttribute
    }


    def 'Can update an attribute'() {
        def importedSurvey = survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        def attribute = findUiTextAttribute('attribute-name', uiRecord)
        attribute.text = 'Updated text'

        when: survey.updateAttribute(attribute)

        then:
        survey.loadSurvey(importedSurvey.name)
        def loadedRecord = survey.selectRecord(uiRecord.getId())
        def loadedAttribute = findUiTextAttribute('attribute-name', loadedRecord)
        loadedAttribute.text == 'Updated text'
    }


    def 'Can lookup node'() {
        survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        def rootEntity = uiRecord.getFirstChild()

        when:
        def lookedUpRootEntity = survey.lookupNode(rootEntity.getId())

        then: lookedUpRootEntity.is rootEntity
    }


    def 'Can get key attributes from record'() {
        survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        def expectedKeyAttribute = findUiTextAttribute('attribute-name', uiRecord)

        expect:
        uiRecord.keyAttributes == [expectedKeyAttribute]
    }


    def 'Record placeholders contains attributes keys'() {
        def importedSurvey = survey.importSurvey(idm)
        survey.addRecord('entity-name')

        when:
        def loadedSurvey = survey.loadSurvey(importedSurvey.name)

        then:
        def recordPlaceholder = loadedSurvey.firstChild.firstChild
        recordPlaceholder.getKeyAttributes().size() == 1
    }


    def 'Record placeholder key attributes are updated when actual attributes are updated'() {
        def importedSurvey = survey.importSurvey(idm)
        def uiRecord = survey.addRecord('entity-name')
        def recordPlaceholder = importedSurvey.firstChild.firstChild as UiRecord.Placeholder
        assert recordPlaceholder.keyAttributes.first().text == null
        def attribute = findUiTextAttribute('attribute-name', uiRecord)
        attribute.text = 'Updated text'

        when:
        survey.updateAttribute(attribute)

        then:
        recordPlaceholder.keyAttributes.first().text == 'Updated text'
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
                    text('attribute-name', 'Attribute label', [key: true])
                    entity('multiple-entity-name', 'Multiple entity label', [multiple: true]) {
                        text('attribute-name2', 'Attribute label2')
                        entity('deeply-nested-entity-name', 'Deeply nested entity label', [multiple: true]) {
                            text('attribute-name3', 'Attribute label3')
                        }
                    }
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
