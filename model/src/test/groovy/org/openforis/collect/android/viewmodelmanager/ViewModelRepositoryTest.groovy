package org.openforis.collect.android.viewmodelmanager

import org.openforis.collect.android.DefinitionProvider
import org.openforis.collect.android.IdGenerator
import org.openforis.collect.android.viewmodel.*
import spock.lang.Specification

import static org.openforis.collect.android.viewmodelmanager.ViewModelRepository.DatabaseViewModelRepository

/**
 * @author Daniel Wiell
 */
class ViewModelRepositoryTest extends Specification {

    def nodes = new DefinitionProviderStub()
    def database = new NodeTestDatabase()
    def repo = new DatabaseViewModelRepository(nodes, new DataSourceNodeRepository(database))

    def cleanup() {
        database.reset()
    }

    def 'Can insert record with attribute'() {
        def record = nodes.addRecord()
        def attribute = nodes.addTextAttribute(record)
        attribute.text = 'The text'

        when:
        repo.insertRecord(record)
        def loadedRecord = repo.recordById(nodes.survey, record.id)

        then:
        assertEquals(record, loadedRecord)
        attribute.text == loadedRecord.firstChild.text
    }

    def 'Can insert record with entity collection'() {
        def record = nodes.addRecord()
        def entityCollection = nodes.addEntityCollection(record)

        when:
        repo.insertRecord(record)
        def loadedRecord = repo.recordById(nodes.survey, record.id)

        then:
        loadedRecord.childCount == 1
        def loadedEntityCollection = loadedRecord.firstChild as UiEntityCollection
        assertEquals(entityCollection, loadedEntityCollection)
        entityCollection.parentEntityId == loadedEntityCollection.parentEntityId
    }

    def 'Can insert entity with attribute'() {
        def record = nodes.addRecord()
        repo.insertRecord(record)
        def entity = nodes.addEntity(record)
        def attribute = nodes.addTextAttribute(entity)
        attribute.text = 'The text'

        when:
        repo.insertEntity(entity)
        def loadedRecord = repo.recordById(nodes.survey, record.id)

        then:
        loadedRecord.childCount == 1
        def loadedEntity = loadedRecord.firstChild as UiEntity
        assertEquals(entity, loadedEntity)
        attribute.text == loadedEntity.firstChild.text
    }


    def 'Can update attribute'() {
        def record = nodes.addRecord()
        def attribute = nodes.addTextAttribute(record)
        repo.insertRecord(record)
        attribute.text = 'Updated'

        when:
        repo.updateAttribute(attribute, [:])
        def loadedRecord = repo.recordById(nodes.survey, record.id)

        then:
        def loadedAttribute = loadedRecord.firstChild as UiTextAttribute
        attribute.text == loadedAttribute.text
    }


    def 'Can load survey record placeholders'() {
        def record = nodes.addRecord()
        repo.insertRecord(record)

        when:
        def records = repo.surveyRecords(nodes.survey.id)

        then:
        records.size() == 1
    }


    private void assertEquals(UiNode n, UiNode n2) {
        n2.with {
            assert n.id == id
            assert n.name == name
            assert n.label == label
            assert n.parent.id == parent.id
        }
    }

    private void assertEquals(UiEntity e, UiEntity e2) {
        assertEquals(e as UiNode, e2 as UiNode)
        assert e.definition.id == e2.definition.id
        assert e.childCount == e2.childCount
    }

    private class DefinitionProviderStub implements DefinitionProvider {
        public static final int SURVEY_ID = IdGenerator.nextId()
        public static final int RECORD_COLLECTION_ID = IdGenerator.nextId()

        private final definitionById = [:]
        final survey = createSurvey()
        final recordCollection = survey.lookupRecordCollection(RECORD_COLLECTION_ID)

        Definition getById(String definitionId) {
            definitionById[definitionId]
        }

        UiRecord addRecord() {
            def definition = definition('record', 'Record')
            def record = new UiRecord(IdGenerator.nextId(), definition, recordCollection)
            survey.addRecord(record)
            return record
        }

        private Definition definition(String name, String label) {
            def definition = new Definition(IdGenerator.nextId().toString(), name, label, false)
            definitionById[definition.id] = definition
            return definition
        }

        private Definition attributeDefinition(String name, String label) {
            def definition = new UiAttributeDefinition(IdGenerator.nextId().toString(), name, label, false)
            definitionById[definition.id] = definition
            return definition
        }

        UiEntity addEntity(UiInternalNode parent) {
            def definition = definition('entity', 'Entity')
            def entity = new UiEntity(IdGenerator.nextId(), true, definition)
            parent.addChild(entity)
            return entity
        }

        UiEntityCollection addEntityCollection(UiEntity parent) {
            def definition = definition('entity-collection', 'Entity Collection')
            def entityCollection = new UiEntityCollection(IdGenerator.nextId(), parent.id, true, definition)
            parent.addChild(entityCollection)
            return entityCollection
        }

        UiTextAttribute addTextAttribute(UiEntity entity) {
            def definition = attributeDefinition('text-uiAttribute', 'Text Attribute')
            def attribute = new UiTextAttribute(IdGenerator.nextId(), true, definition)
            entity.addChild(attribute)
            return attribute
        }

        private UiSurvey createSurvey() {
            def survey = new UiSurvey(SURVEY_ID, definition('survey', 'Survey'))
            survey.addChild(new UiRecordCollection(RECORD_COLLECTION_ID, definition('record', 'Collection')))
            return survey
        }
    }
}
