package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.NodeEvent
import org.openforis.collect.android.SurveyListener
import org.openforis.collect.android.viewmodel.*
import org.openforis.collect.android.viewmodelmanager.NodeTestDatabase
import spock.lang.Specification

import static org.openforis.collect.android.IdGenerator.nextId
import static org.openforis.collect.android.collectadapter.TestCollectModelFactory.surveyService

/**
 * @author Daniel Wiell
 */
class CodeListTest extends Specification {
    @Delegate
    IdmBuilder builder = new IdmBuilder()
    def listener = new Listener()
    def nodeDatabase = new NodeTestDatabase()
    def modelDatabase = new ModelTestDatabase()

    CollectModelBackedSurveyService surveyService

    UiRecord uiRecord
    UiCodeAttribute parentAttribute
    UiCodeAttribute childAttribute

    def setup() {
        surveyService = surveyService(nodeDatabase, modelDatabase)
        surveyService.listener = listener
        surveyService.importSurvey(idm)
        uiRecord = surveyService.addRecord('entity-name')
        parentAttribute = findNodeByName(uiRecord, 'parent') as UiCodeAttribute
        childAttribute = findNodeByName(uiRecord, 'child') as UiCodeAttribute
    }

    def cleanup() {
        nodeDatabase.reset()
        modelDatabase.reset()
    }

    def 'Parent code can be set'() {
        parentAttribute.setCode(uiCode('001'))

        when:
        surveyService.updateAttribute(parentAttribute)

        then:
        !errors()
    }

    def 'Child code can be set if parent code is set'() {
        parentAttribute.setCode(uiCode('001'))
        surveyService.updateAttribute(parentAttribute)
        childAttribute.setCode(uiCode('001a'))

        when:
        surveyService.updateAttribute(childAttribute)

        then:
        !errors()
    }


    def 'Setting child code when parent not set gives validation error'() {
        childAttribute.setCode(uiCode('001a'))

        when:
        surveyService.updateAttribute(childAttribute)

        then:
        errors(childAttribute).size() == 1
        errors(parentAttribute).empty
    }

    def 'Missing required code in child gives validation error on child and not parent'() {
        childAttribute.setCode(uiCode('001a'))
        surveyService.updateAttribute(childAttribute)
        childAttribute.setCode(null)
        listener.changeEvents.clear()

        when:
        surveyService.updateAttribute(childAttribute)

        then:
        errors(childAttribute).size() == 1
        errors(parentAttribute).empty
    }

    Set<UiValidationError> errors(UiAttribute attribute) {
        def attributeChangeEvents = listener.changeEvents
        return attributeChangeEvents.collect {
            it.changes[attribute]?.validationErrors
        }.findAll()
    }

    List<UiValidationError> errors() {
        listener.changeEvents.collect { it.validationErrors }.findAll()
    }

    UiCode uiCode(String code) {
        new UiCode(code, null)
    }

    InputStream getIdm() {
        idmXmlStream {
            project('Project label')
            codeLists {
                list(id: nextId(), name: 'list') {
                    label(type: 'list', 'List')
                    hierarchy {
                        level(name: 'parent') {
                            label('Parent')
                        }
                        level(name: 'child') {
                            label('Child')
                        }
                    }
                    items {
                        item(id: nextId()) {
                            code('001')
                            label('First Parent')
                            item(id: nextId()) {
                                code('001a')
                                label('First Child A')
                            }
                        }
                        item(id: nextId()) {
                            code('002')
                            label('Second Parent')
                            item(id: nextId()) {
                                code('002a')
                                label('Second Child A')
                            }
                            item(id: nextId()) {
                                code('002b')
                                label('Second Child B')
                            }
                        }
                    }
                }
            }
            schema {
                entity('entity-name', 'Entity label') {
                    code(id: nextId(), list: 'list', name: 'parent', required: true) {
                        label('Parent Code')
                    }
                    code(id: nextId(), list: 'list', name: 'child', parent: 'parent', required: true) {
                        label('Child Code')
                    }
                }
            }
        }
    }

    UiNode findNodeByName(UiNode node, String name) {
        if (node.name == name)
            return node
        else if (node instanceof UiInternalNode)
            return node.children.collect { findNodeByName(it, name) }.find()
        return null
    }

    private static class Listener implements SurveyListener {
        final List<Map<String, UiNode>> selectNodeEvents = []
        final List<Map<String, ?>> changeEvents = []

        void onNodeSelected(UiNode previous, UiNode selected) {
            selectNodeEvents.add(previous: previous, selected: selected)
        }

        void onNodeChanged(NodeEvent event, UiNode node, Map<UiNode, UiNodeChange> changes) {
            changeEvents.add(node: node, changes: changes)
        }
    }
}
