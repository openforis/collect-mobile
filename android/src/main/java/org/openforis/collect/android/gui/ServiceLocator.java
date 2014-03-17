package org.openforis.collect.android.gui;

import android.content.Context;
import android.os.Environment;
import liquibase.database.core.AndroidSQLiteDatabase;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.*;
import org.openforis.collect.android.databaseschema.ModelDatabaseSchemaUpdater;
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.NodeSchemaChangeLog;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodel.UiSurvey;
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository;
import org.openforis.collect.android.viewmodelmanager.TaxonService;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.persistence.DynamicTableDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.openforis.collect.android.viewmodelmanager.ViewModelRepository.DatabaseViewModelRepository;

/**
 * @author Daniel Wiell
 */
public class ServiceLocator {
    private static CollectModelManager collectModelManager;
    private static SurveyService surveyService;
    private static TaxonService taxonService;

    public static void init(Context applicationContext) {
        if (surveyService == null) {
            AndroidDatabase modelDatabase = new AndroidDatabase(applicationContext, "model");
            collectModelManager = createCollectModelManager(modelDatabase, applicationContext);
            surveyService = createSurveyService(applicationContext, collectModelManager);
            loadOrImportSurvey(surveyService);
            taxonService = createTaxonService(modelDatabase);
        }
    }

    public static SurveyService surveyService() {
        return surveyService;
    }

    public static CodeListService codeListService() {
        return collectModelManager;
    }

    public static TaxonService taxonService() {
        return taxonService;
    }

    private static void loadOrImportSurvey(SurveyService surveyService) {
        UiSurvey uiSurvey = surveyService.loadSurvey("survey");
//        UiSurvey uiSurvey = surveyService.loadSurvey("naforma1");
//        UiSurvey uiSurvey = surveyService.loadSurvey("http://www.openforis.org/idm/naforma1");
        if (uiSurvey == null)
            surveyService.importSurvey(idmXmlStream());
    }


    public static CollectModelBackedSurveyService createSurveyService(Context context, CollectModelManager collectModelManager) {
        AndroidDatabase nodeDatabase = new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                context,
                "nodes"
        );
        return new CollectModelBackedSurveyService(
                new ViewModelManager(
                        new DatabaseViewModelRepository(collectModelManager, new DataSourceNodeRepository(nodeDatabase))
                ),
                collectModelManager
        );
    }

    private static CollectModelManager createCollectModelManager(AndroidDatabase modelDatabase, Context context) {
        new ModelDatabaseSchemaUpdater().update(modelDatabase, new AndroidSQLiteDatabase());

        DatabaseExternalCodeListProvider externalCodeListProvider = createExternalCodeListProvider(modelDatabase);
        CodeListManager codeListManager = new MeteredCodeListManager(new MobileCodeListItemDao(modelDatabase),
                externalCodeListProvider
        );
        SurveyManager surveyManager = new MeteredSurveyManager(codeListManager, externalCodeListProvider, modelDatabase);
        RecordManager recordManager = new MeteredRecordManager(codeListManager, surveyManager);
        return new CollectModelManager(surveyManager, recordManager, codeListManager, modelDatabase);
    }

    private static DatabaseExternalCodeListProvider createExternalCodeListProvider(AndroidDatabase modelDatabase) {
        DatabaseExternalCodeListProvider externalCodeListProvider = new MobileExternalCodeListProvider(modelDatabase);
        DynamicTableDao dynamicTableDao = new DynamicTableDao();
        dynamicTableDao.setDataSource(modelDatabase.dataSource());
        externalCodeListProvider.setDynamicTableDao(dynamicTableDao);
        return externalCodeListProvider;
    }


    private static TaxonService createTaxonService(Database modelDatabase) {
        return new TaxonRepository(modelDatabase);
    }

    private static synchronized InputStream idmXmlStream() {
        File idmFile = getIdmFile();
        try {
            return new FileInputStream(idmFile);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    private static File getIdmFile() {
        File storageDir = Environment.getExternalStorageDirectory();
        File collectDir = new File(storageDir, "Collect");
        if (!collectDir.exists())
            if (!collectDir.mkdir())
                throw new IllegalStateException("Failed to create dir: " + collectDir);
        return new File(collectDir, "idm.xml");
    }
}
