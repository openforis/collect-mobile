package org.openforis.collect.android.collectadapter

import org.openforis.collect.android.IdGenerator
import org.openforis.collect.model.CollectSurvey
import org.openforis.collect.model.SurveySummary
import org.openforis.collect.persistence.SurveyDao
import org.openforis.collect.persistence.SurveyImportException
import org.openforis.idm.metamodel.Survey

/**
 * @author Daniel Wiell
 */
class SurveyDaoStub extends SurveyDao {
    private final Map surveyByName = [:];

    List<CollectSurvey> loadAll() {
        return new ArrayList(surveyByName.values())
    }

    @Override
    List<CollectSurvey> loadAllPublished() {
        return loadAll()
    }

    void insert(CollectSurvey survey) throws SurveyImportException {
        survey.id = IdGenerator.nextId()
        surveyByName[survey.name] = survey
    }
}

