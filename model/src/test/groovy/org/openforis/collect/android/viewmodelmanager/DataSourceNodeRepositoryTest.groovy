package org.openforis.collect.android.viewmodelmanager

import spock.lang.Specification

import static org.openforis.collect.android.viewmodelmanager.NodeDto.Type.RECORD

/**
 * @author Daniel Wiell
 */
class DataSourceNodeRepositoryTest extends Specification {
    def database = new NodeTestDatabase()
    def repo = new DataSourceNodeRepository(database)

    def cleanup() {
        database.reset()
    }

    def 'Can insert nodes'() {
        def rootEntity = new NodeDto(id: 1, status: 'OK', definitionId: 'definition id', surveyId: 2, recordId: 3, recordCollectionName: 'name', type: RECORD)
        when:
        repo.insert([rootEntity])
        def nodes = repo.recordNodes(3)

        then:
        nodes.rootNode.id == 1
        nodes.rootNode.definitionId == 'definition id'
        nodes.rootNode.surveyId == 2
        nodes.rootNode.recordId == 3
        nodes.rootNode.recordCollectionName == 'name'
    }
}
