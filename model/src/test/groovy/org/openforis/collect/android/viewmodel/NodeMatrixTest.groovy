package org.openforis.collect.android.viewmodel

import spock.lang.Specification

class NodeMatrixTest extends Specification {
    def id = 0
    def record = record()

    def 'Matrix for record contains one row'() {
        def matrix = new NodeMatrix(record)

        expect:
            matrix.rowCount() == 1
    }

    def 'Matrix for entity with a sibbling contains two rows'() {
        UiEntityCollection entityCollection = entityCollection()
        UiEntity entity = entityIn(entityCollection)
        entityIn(entityCollection)
        def matrix = new NodeMatrix(entity)

        expect:
            matrix.rowCount() == 2
    }

    def 'Can get rows'() {
        def entityCollection = entityCollection()
        def entity = entityIn(entityCollection)
        def sibling = entityIn(entityCollection)

        def matrix = new NodeMatrix(entity)

        expect:
            matrix.rows() == [entity, sibling]
    }


    def 'Include rows for siblings when nested entity'() {
        def entityCollection = entityCollection()
        def entity = entityIn(entityCollection)
        def nestedEntity = entityIn(entity, 'nested')
        def sibling = entityIn(entityCollection)
        def nestedSibling = entityIn(sibling, 'nested')

        def matrix = new NodeMatrix(nestedEntity)

        expect:
            matrix.rows() == [nestedEntity, nestedSibling]
    }

    def 'Rows for entity directly under record only contains entity'() {
        def entity = entityIn(record)
        def matrix = new NodeMatrix(entity)

        expect:
            matrix.rows() == [entity]
    }

    def 'Header row contains definitions'() {
        def entity = entityIn(entityCollection())
        def definitions = [definition('A'), definition('B')]
        definitions.each { attributeIn(entity, it) }

        def matrix = new NodeMatrix(entity)

        expect:
            matrix.headerRow() == definitions
    }

    def 'Can determine row index of an entity'() {
        def entityCollection = entityCollection()
        def entity = entityIn(entityCollection)
        def sibling = entityIn(entityCollection)

        def matrix = new NodeMatrix(entity)

        expect:
            matrix.rowIndex(entity) == 0
            matrix.rowIndex(sibling) == 1
    }

    def 'Can determine column index of an attriute'() {
        def entityCollection = entityCollection()
        def entity = entityIn(entityCollection)
        def sibling = entityIn(entityCollection)
        def firstAttribute = attributeIn(entity, definition('first'))
        def secondAttribute = attributeIn(entity, definition('second'))

        def matrix = new NodeMatrix(entity)

        expect:
            matrix.columnIndex(firstAttribute) == 0
            matrix.columnIndex(secondAttribute) == 1
    }

    private UiEntity entityIn(UiInternalNode internalNode, String name = 'entity') {
        entityIn(internalNode, new Definition(id as String, name, name, true))
    }

    private UiEntity entityIn(UiInternalNode internalNode, Definition definition) {
        def id = nextId
        def entity = new UiEntity(id, true, definition)
        internalNode.addChild(entity)
        return entity
    }

    private UiAttribute attributeIn(UiEntity entity, UiAttributeDefinition definition) {
        def id = nextId
        def attribute = new UiTextAttribute(id, true, definition)
        entity.addChild(attribute)
        return attribute
    }

    private UiAttributeDefinition definition(String name) {
        new UiAttributeDefinition(nextId as String, name, name, true)
    }


    private UiEntityCollection entityCollection(String name = 'entityCollection') {
        def id = nextId
        def entityCollection = new UiEntityCollection(id, record.id, true, new Definition(id as String, name, name,
                true))
        record.addChild(entityCollection)
        return entityCollection
    }

    private UiRecord record() {
        def id = nextId
        def definition = new Definition(id as String, 'record', 'record', true)
        new UiRecord(id, definition, null, new UiRecord.Placeholder(1, null, null, definition, [], new Date(), new Date()))
    }


    int getNextId() {
        ++id
    }
}
