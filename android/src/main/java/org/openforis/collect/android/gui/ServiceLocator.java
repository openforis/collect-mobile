package org.openforis.collect.android.gui;

import android.content.Context;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.*;
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog;
import org.openforis.collect.android.gui.util.WorkingDir;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.NodeSchemaChangeLog;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository;
import org.openforis.collect.android.viewmodelmanager.TaxonService;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.persistence.DynamicTableDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.idm.model.expression.ExpressionFactory;

import java.io.File;

import static org.openforis.collect.android.viewmodelmanager.ViewModelRepository.DatabaseViewModelRepository;

/**
 * @author Daniel Wiell
 */
public class ServiceLocator {

    private static final String MODEL_DB = "collect.db";
    private static final String NODES_DB = "nodes";
    private static CollectModelManager collectModelManager;
    private static SurveyService surveyService;
    private static TaxonService taxonService;
    private static File workingDir;
    private static AndroidDatabase modelDatabase;
    private static AndroidDatabase nodeDatabase;

    public static boolean init(Context applicationContext) {
        if (surveyService == null) {
            workingDir = initWorkingDir(applicationContext);
            if (!isSurveyImported(applicationContext))
                return false;
            modelDatabase = createModelDatabase(applicationContext);
            nodeDatabase = createNodeDatabase(applicationContext);
            new ModelDatabaseMigrator(modelDatabase, applicationContext).migrateIfNeeded();
            collectModelManager = createCollectModelManager(modelDatabase, nodeDatabase);
            SurveyService surveyService = createSurveyService(collectModelManager, nodeDatabase);
            surveyService.loadSurvey();
            taxonService = createTaxonService(modelDatabase);
            ServiceLocator.surveyService = surveyService;
        }
        return true;
    }

    public static void reset() {
        surveyService = null;
        modelDatabase.close();
        nodeDatabase.close();
    }

    private static File initWorkingDir(Context applicationContext) {
        File dir = WorkingDir.root(applicationContext);
        if ((!dir.exists() && !dir.mkdirs()) || !dir.canWrite())
            throw new WorkingDirNotWritable();
        return dir;
    }

    private static File databasePath(String databaseName, Context context) {
        return new File(WorkingDir.databases(context), databaseName);
    }

    public static void importSurvey(String surveyDatabasePath, Context applicationContext) throws MalformedSurvey, WrongSurveyVersion {
        new SurveyImporter(surveyDatabasePath, applicationContext, databasePath(MODEL_DB, applicationContext)).importSurvey();
    }

    public static void importDefaultSurvey(Context context) {
        SurveyImporter.importDefaultSurvey(databasePath(MODEL_DB, context.getApplicationContext()), context);
    }

    public static void recreateNodeDatabase(Context applicationContext) {
        deleteDatabase(NODES_DB, nodeDatabase, applicationContext);
        createNodeDatabase(applicationContext);
    }

    public static void deleteModelDatabase(Context applicationContext) {
        deleteDatabase(MODEL_DB, modelDatabase, applicationContext);
    }

    private static void deleteDatabase(String databaseName, AndroidDatabase database, Context applicationContext) {
        if (database != null) {
            database.close();
            File nodesDbPath = databasePath(databaseName, applicationContext);
            nodesDbPath.delete();
        }
    }

    public static boolean isSurveyImported(Context context) {
        return context.getDatabasePath(databasePath(MODEL_DB, context).getAbsolutePath()).exists();
    }

    private static AndroidDatabase createModelDatabase(Context applicationContext) {
        return new AndroidDatabase(applicationContext, databasePath(MODEL_DB, applicationContext));
    }

    private static AndroidDatabase createNodeDatabase(Context applicationContext) {
        return new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                applicationContext,
                databasePath(NODES_DB, applicationContext)
        );
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

    public static CollectModelBackedSurveyService createSurveyService(CollectModelManager collectModelManager, Database database) {
        return new CollectModelBackedSurveyService(
                new ViewModelManager(
                        new DatabaseViewModelRepository(collectModelManager, new DataSourceNodeRepository(database))
                ),
                collectModelManager, exportFile()
        );
    }

    private static File exportFile() {
        return new File(workingDir, "survey_export.zip");
    }

    private static CollectModelManager createCollectModelManager(AndroidDatabase modelDatabase, Database nodeDatabase) {
        DatabaseExternalCodeListProvider externalCodeListProvider = createExternalCodeListProvider(modelDatabase);
//        CodeListManager codeListManager = new MeteredCodeListManager(new MobileCodeListItemDao(modelDatabase),

        CodeListManager codeListManager = new CodeListManager();
        codeListManager.setCodeListItemDao(new MobileCodeListItemDao(modelDatabase));
        codeListManager.setExternalCodeListProvider(externalCodeListProvider);

//        MeteredValidator validator = new MeteredValidator(codeListManager);
        CollectValidator validator = new CollectValidator();
        validator.setCodeListManager(codeListManager);
//        SurveyManager surveyManager = new MeteredSurveyManager(codeListManager, validator, externalCodeListProvider, modelDatabase);
        SurveyManager surveyManager = new SurveyManager();
        ExpressionFactory expressionFactory = new ExpressionFactory();
        expressionFactory.setLookupProvider(new MobileDatabaseLookupProvider(modelDatabase));
        CollectSurveyContext collectSurveyContext = new CollectSurveyContext(expressionFactory, validator);
        collectSurveyContext.setExternalCodeListProvider(externalCodeListProvider);
        SurveyDao surveyDao = new SurveyDao();
        surveyDao.setSurveyContext(collectSurveyContext);
        surveyDao.setDataSource(modelDatabase.dataSource());
        surveyManager.setSurveyDao(surveyDao);
        surveyManager.setCodeListManager(codeListManager);
        surveyManager.setCollectSurveyContext(collectSurveyContext);
        surveyDao.setSurveyContext(collectSurveyContext);
        surveyManager.setSurveyDao(surveyDao);


        RecordManager recordManager = new MobileRecordManager(
                codeListManager,
                surveyManager,
                new RecordUniquenessChecker.DataSourceRecordUniquenessChecker(nodeDatabase)
        );
        validator.setRecordManager(recordManager);

//        RecordManager meteredRecordManager = new MeteredRecordManager(recordManager);
//        return new CollectModelManager(surveyManager, meteredRecordManager, codeListManager, modelDatabase);
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

}
