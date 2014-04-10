package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.manager.*;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.expression.ExpressionFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Daniel Wiell
 */
public class MeteredSurveyManager extends SurveyManager {
    private final SurveyManager delegate;

    public MeteredSurveyManager(CodeListManager codeListManager, Validator validator, ExternalCodeListProvider externalCodeListProvider, Database database) {
        delegate = new SurveyManager();
        CollectSurveyContext collectSurveyContext = new CollectSurveyContext(new ExpressionFactory(), validator);
        collectSurveyContext.setExternalCodeListProvider(externalCodeListProvider);
        SurveyDao surveyDao = new SurveyDao();
        surveyDao.setSurveyContext(collectSurveyContext);
        surveyDao.setDataSource(database.dataSource());
        delegate.setSurveyDao(surveyDao);
        delegate.setCodeListManager(codeListManager);
        delegate.setCollectSurveyContext(collectSurveyContext);
        surveyDao.setSurveyContext(collectSurveyContext);
        delegate.setSurveyDao(surveyDao);
    }

    public void init() {
        time("init", new Runnable() {
            public void run() {
                delegate.init();
            }
        });
    }

    public List<CollectSurvey> getAll() {
        return time("getAll", new Callable<List<CollectSurvey>>() {
            public List<CollectSurvey> call() throws Exception {
                return delegate.getAll();
            }
        });
    }

    public CollectSurvey get(final String name) {
        return time("get", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.get(name);
            }
        });
    }

    public CollectSurvey getById(final int id) {
        return time("getById", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.getById(id);
            }
        });
    }

    public CollectSurvey getByUri(final String uri) {
        return time("getByUri", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.getByUri(uri);
            }
        });
    }

    public CollectSurvey importWorkModel(final InputStream is, final String name, final boolean validate) throws SurveyImportException, SurveyValidationException {
        return time("importWorkModel(is, name, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.importWorkModel(is, name, validate);
            }
        });
    }

    public CollectSurvey importWorkModel(final File surveyFile, final String name, final boolean validate) throws SurveyImportException, SurveyValidationException {
        return time("importWorkModel(surveyFile, name, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.importWorkModel(surveyFile, name, validate);
            }
        });
    }

    public CollectSurvey importInPublishedWorkModel(final String uri, final File surveyFile, final boolean validate) throws SurveyImportException, SurveyValidationException {
        return time("importInPublishedWorkModel", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.importInPublishedWorkModel(uri, surveyFile, validate);
            }
        });
    }

    public CollectSurvey importModel(final InputStream is, final String name, final boolean validate) throws SurveyImportException, SurveyValidationException {
        return time("importModel(is, name, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.importModel(is, name, validate);
            }
        });
    }

    public CollectSurvey importModel(final File surveyFile, final String name, final boolean validate) throws SurveyImportException, SurveyValidationException {
        return time("importModel(surveyFile, name, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.importModel(surveyFile, name, validate);
            }
        });
    }

    public CollectSurvey updateModel(final InputStream is, final boolean validate) throws IdmlParseException, SurveyValidationException, SurveyImportException {
        return time("updateModel(is, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.updateModel(is, validate);
            }
        });
    }

    public CollectSurvey updateModel(final File surveyFile, final boolean validate) throws SurveyValidationException, SurveyImportException {
        return time("updateModel(surveyFile, validate)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.updateModel(surveyFile, validate);
            }
        });
    }

    public void importModel(CollectSurvey survey) throws SurveyImportException {
        throw new UnsupportedOperationException("Deprecated");
    }

    public void updateModel(CollectSurvey survey) throws SurveyImportException {
        throw new UnsupportedOperationException("Deprecated");
    }

    public List<SurveySummary> getSurveySummaries(final String lang) {
        return time("getSurveySummaries", new Callable<List<SurveySummary>>() {
            public List<SurveySummary> call() throws Exception {
                return delegate.getSurveySummaries(lang);
            }
        });
    }

    public SurveySummary getPublishedSummaryByUri(final String uri) {
        return time("getPublishedSummaryByUri", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.getPublishedSummaryByUri(uri);
            }
        });
    }

    public SurveySummary getPublishedSummaryByName(final String name) {
        return time("getPublishedSummaryByName", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.getPublishedSummaryByName(name);
            }
        });
    }

    public String marshalSurvey(final Survey survey) {
        return time("marshalSurvey(survey)", new Callable<String>() {
            public String call() throws Exception {
                return delegate.marshalSurvey(survey);
            }
        });
    }

    public void marshalSurvey(final Survey survey, final OutputStream os) {
        time("marshalSurvey(survey, os)", new Runnable() {
            public void run() {
                delegate.marshalSurvey(survey, os);
            }
        });
    }

    public void marshalSurvey(final Survey survey, final OutputStream os, final boolean marshalCodeLists, final boolean marshalPersistedCodeLists, final boolean marshalExternalCodeLists) {
        time("marshalSurvey(survey, os, marshalCodeLists, marshalPersistedCodeLists, marshalExternalCodeLists)", new Runnable() {
            public void run() {
                delegate.marshalSurvey(survey, os, marshalCodeLists, marshalPersistedCodeLists, marshalExternalCodeLists);
            }
        });
    }

    public CollectSurvey unmarshalSurvey(final InputStream is) throws IdmlParseException, SurveyValidationException {
        return time("unmarshalSurvey(is)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.unmarshalSurvey(is);
            }
        });
    }

    public CollectSurvey unmarshalSurvey(final File surveyFile, final boolean validate, final boolean includeCodeListItems) throws IdmlParseException, SurveyValidationException {
        return time("unmarshalSurvey(surveyFile, validate, includeCodeListItems)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.unmarshalSurvey(surveyFile, validate, includeCodeListItems);
            }
        });
    }

    public CollectSurvey unmarshalSurvey(final InputStream is, final boolean validate, final boolean includeCodeListItems) throws IdmlParseException, SurveyValidationException {
        return time("unmarshalSurvey(is, validate, includeCodeListItems)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.unmarshalSurvey(is, validate, includeCodeListItems);
            }
        });
    }

    public CollectSurvey unmarshalSurvey(final Reader reader) throws IdmlParseException, SurveyValidationException {
        return time("unmarshalSurvey(reader)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.unmarshalSurvey(reader);
            }
        });
    }

    public CollectSurvey unmarshalSurvey(final Reader reader, final boolean validate, final boolean includeCodeListItems) throws IdmlParseException, SurveyValidationException {
        return time("unmarshalSurvey(reader, validate, includeCodeListItems)", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.unmarshalSurvey(reader, validate, includeCodeListItems);
            }
        });
    }

    public List<SurveySummary> loadSummaries() {
        return time("loadSummaries", new Callable<List<SurveySummary>>() {
            public List<SurveySummary> call() throws Exception {
                return delegate.loadSummaries();
            }
        });
    }

    public SurveySummary loadSummaryByUri(final String uri) {
        return time("loadSummaryByUri", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.loadSummaryByUri(uri);
            }
        });
    }

    public SurveySummary loadSummaryByName(final String name) {
        return time("loadSummaryByName", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.loadSummaryByName(name);
            }
        });
    }

    public CollectSurvey loadSurveyWork(final int id) {
        return time("loadSurveyWork", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.loadSurveyWork(id);
            }
        });
    }

    public SurveySummary loadWorkSummaryByUri(final String uri) {
        return time("loadWorkSummaryByUri", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.loadWorkSummaryByUri(uri);
            }
        });
    }

    public SurveySummary loadWorkSummaryByName(final String name) {
        return time("loadWorkSummaryByName", new Callable<SurveySummary>() {
            public SurveySummary call() throws Exception {
                return delegate.loadWorkSummaryByName(name);
            }
        });
    }

    public boolean isSurveyWork(final CollectSurvey survey) {
        return time("isSurveyWork", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.isSurveyWork(survey);
            }
        });
    }

    public CollectSurvey createSurveyWork() {
        return time("createSurveyWork", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.createSurveyWork();
            }
        });
    }

    public void saveSurveyWork(final CollectSurvey survey) throws SurveyImportException {
        time("saveSurveyWork", new Callable<Void>() {
            public Void call() throws SurveyImportException {
                delegate.saveSurveyWork(survey);
                return null;
            }
        });
    }

    public CollectSurvey duplicatePublishedSurveyForEdit(final String uri) {
        return time("duplicatePublishedSurveyForEdit", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return delegate.duplicatePublishedSurveyForEdit(uri);
            }
        });
    }

    public void publish(final CollectSurvey survey) throws SurveyImportException {
        time("publish", new Callable<Void>() {
            public Void call() throws Exception {
                delegate.publish(survey);
                return null;
            }
        });
    }

    public void cancelRecordValidation(final int surveyId) {
        time("cancelRecordValidation", new Runnable() {
            public void run() {
                delegate.cancelRecordValidation(surveyId);
            }
        });
    }

    public void validateRecords(final int surveyId, final User user) {
        time("validateRecords", new Runnable() {
            public void run() {
                delegate.validateRecords(surveyId, user);
            }
        });
    }

    public void deleteSurvey(final int id) {
        time("deleteSurvey", new Runnable() {
            public void run() {
                delegate.deleteSurvey(id);
            }
        });
    }

    public void deleteSurveyWork(final Integer id) {
        time("deleteSurveyWork", new Runnable() {
            public void run() {
                delegate.deleteSurveyWork(id);
            }
        });
    }

    public boolean isRecordValidationInProgress(final int surveyId) {
        return time("isRecordValidationInProgress", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.isRecordValidationInProgress(surveyId);
            }
        });
    }

    public SamplingDesignManager getSamplingDesignManager() {
        return delegate.getSamplingDesignManager();
    }

    public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
        delegate.setSamplingDesignManager(samplingDesignManager);
    }

    public SpeciesManager getSpeciesManager() {
        return delegate.getSpeciesManager();
    }

    public void setSpeciesManager(SpeciesManager speciesManager) {
        delegate.setSpeciesManager(speciesManager);
    }

    public SurveyDao getSurveyDao() {
        return delegate.getSurveyDao();
    }

    public void setSurveyDao(SurveyDao surveyDao) {
        delegate.setSurveyDao(surveyDao);
    }

    public SurveyWorkDao getSurveyWorkDao() {
        return delegate.getSurveyWorkDao();
    }

    public void setSurveyWorkDao(SurveyWorkDao surveyWorkDao) {
        delegate.setSurveyWorkDao(surveyWorkDao);
    }

    public CollectSurveyContext getCollectSurveyContext() {
        return delegate.getCollectSurveyContext();
    }

    public void setCollectSurveyContext(CollectSurveyContext collectSurveyContext) {
        delegate.setCollectSurveyContext(collectSurveyContext);
    }

    public CodeListManager getCodeListManager() {
        return delegate.getCodeListManager();
    }

    public void setCodeListManager(CodeListManager codeListManager) {
        delegate.setCodeListManager(codeListManager);
    }

    public SurveyValidator getSurveyValidator() {
        return delegate.getSurveyValidator();
    }

    public void setSurveyValidator(SurveyValidator validator) {
        delegate.setSurveyValidator(validator);
    }

    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(SurveyManager.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(SurveyManager.class, methodName, action);
    }
}
