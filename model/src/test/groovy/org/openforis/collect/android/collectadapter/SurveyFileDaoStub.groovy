package org.openforis.collect.android.collectadapter


import org.openforis.collect.model.CollectSurvey
import org.openforis.collect.model.SurveyFile
import org.openforis.collect.persistence.SurveyFileDao

class SurveyFileDaoStub extends SurveyFileDao {

    @Override
    List<SurveyFile> loadBySurvey(CollectSurvey survey) {
        return Collections.emptyList()
    }

    @Override
    SurveyFile loadById(Integer id) {
        return null
    }

    @Override
    byte[] loadContent(SurveyFile item) {
        return null
    }
}

