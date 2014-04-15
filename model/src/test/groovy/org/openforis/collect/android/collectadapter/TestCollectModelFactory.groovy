package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.util.persistence.Database
import org.openforis.collect.io.exception.CodeListImportException
import org.openforis.collect.manager.CodeListManager
import org.openforis.collect.manager.RecordManager
import org.openforis.collect.manager.SurveyManager
import org.openforis.collect.model.CollectSurvey
import org.openforis.collect.model.CollectSurveyContext
import org.openforis.collect.model.validation.CollectValidator
import org.openforis.idm.metamodel.CodeList
import org.openforis.idm.metamodel.CodeListItem
import org.openforis.idm.metamodel.validation.Validator
import org.openforis.idm.model.expression.ExpressionFactory

/**
 * @author Daniel Wiell
 */
class TestCollectModelFactory {
    static CollectModelManager collectModelManager(Database database) {
        new CollectModelManager(surveyManager, recordManager, null, database) // TODO: Ugly with the nulls
    }

    public static RecordManager getRecordManager() {
        new RecordManager(false)
    }


    public static SurveyManager getSurveyManager() {
        return surveyManager(new CodeListManagerStub())
    }

    public static SurveyManager surveyManager(CodeListManager codeListManager, Validator validator = new Validator()) {
        def manager = new SurveyManager()
        manager.surveyDao = new SurveyDaoStub()
        CollectSurveyContext context = new CollectSurveyContext(new ExpressionFactory(), validator);
        manager.surveyDao.surveyContext = context;
        manager.collectSurveyContext = context;
        manager.codeListManager = codeListManager
        return manager
    }

    public static Validator collectValidator(CodeListManager codeListManager, RecordManager recordManager) {
        new CollectValidator(codeListManager: codeListManager, recordManager: recordManager)
    }

    private static class CodeListManagerStub extends CodeListManager {
        public void importCodeLists(CollectSurvey survey, File surveyFile) throws CodeListImportException {}

        public <T extends CodeListItem> List<T> loadRootItems(CodeList list) {
            return Collections.emptyList();
        }
    }
}
