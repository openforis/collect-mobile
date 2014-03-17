package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.IdGenerator
import org.openforis.collect.model.CollectSurvey
import org.openforis.idm.metamodel.CodeAttributeDefinition
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class CodeListSizeEvaluatorTest extends Specification {
    public static final int EXPECTED_CODE_LIST_SIZE = 123
    @Delegate IdmBuilder builder = new IdmBuilder()
    final survey = survey()
    final codeDefinitions = survey.schema.getRootEntityDefinition('root').getChildDefinitions() as List<CodeAttributeDefinition>
    def rootCodeDefinition = codeDefinitions[0]
    def childCodeDefinition = codeDefinitions[1]
    def externalDefinition = codeDefinitions[2]
    int codeListId
    int externalCodeListId
    def codeListSizeDao = Mock(CodeListSizeDao)

    def evaluator = new CodeListSizeEvaluator(codeListSizeDao)

    def 'Loads size of external code list'() {
        when:
        def size = evaluator.size(externalDefinition)

        then:
        1 * codeListSizeDao.externalCodeListSize(externalCodeListId, 1) >> EXPECTED_CODE_LIST_SIZE
        size == EXPECTED_CODE_LIST_SIZE
    }

    def 'Loads size of root code list'() {
        when:
        def size = evaluator.size(rootCodeDefinition)

        then:
        1 * codeListSizeDao.codeListSize(codeListId, 1) >> EXPECTED_CODE_LIST_SIZE
        size == EXPECTED_CODE_LIST_SIZE
    }

    def 'Loads size of child code list'() {
        when:
        def size = evaluator.size(childCodeDefinition)

        then:
        1 * codeListSizeDao.codeListSize(codeListId, 2) >> EXPECTED_CODE_LIST_SIZE
        size == EXPECTED_CODE_LIST_SIZE
    }

    CollectSurvey survey() {
        def idm = idmXmlStream {
            codeLists {
                list(id: codeListId = IdGenerator.nextId(), name: 'list') {
                    label(type: 'item', 'Hierarchy')
                    codingScheme(scope: 'local')
                    hierarchy {
                        level(name: 'root_code') {
                            label('Root Code')
                        }
                        level(name: 'child_code') {
                            label('Child Code')
                        }
                    }
                }
                list(id: externalCodeListId = IdGenerator.nextId(), name: 'external', lookup: 'external') {
                    label(type: 'item', 'Hierarchy')
                    codingScheme(scope: 'local')
                }
            }
            schema {
                entity('root', 'Root') {
                    code(id: IdGenerator.nextId(), name: 'root_code_attribute', list: 'list') {
                        label('Root Code Attribute')
                    }
                    code(id: IdGenerator.nextId(), name: 'child_code_attribute', list: 'list', parent: 'root_code_attribute') {
                        label('Child Code Attribute')
                    }
                    code(id: IdGenerator.nextId(), name: 'external_attribute', list: 'external') {
                        label('External Attribute')
                    }
                }
            }
        }

        def manager = TestCollectModelFactory.surveyManager
        return manager.importModel(idm, "survey", false)
    }

}
