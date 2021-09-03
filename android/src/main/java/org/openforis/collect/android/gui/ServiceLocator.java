package org.openforis.collect.android.gui;

import android.content.Context;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.CoordinateDestinationService;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.CollectModelBackedSurveyService;
import org.openforis.collect.android.collectadapter.CollectModelManager;
import org.openforis.collect.android.collectadapter.MobileExternalCodeListProvider;
import org.openforis.collect.android.collectadapter.MobileRecordManager;
import org.openforis.collect.android.collectadapter.RecordUniquenessChecker;
import org.openforis.collect.android.collectadapter.TaxonRepository;
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog;
import org.openforis.collect.android.gui.settings.SettingsActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.NodeSchemaChangeLog;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository;
import org.openforis.collect.android.viewmodelmanager.TaxonService;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.persistence.DatabaseLookupProvider;
import org.openforis.collect.persistence.DynamicTableDao;
import org.openforis.collect.persistence.SamplingDesignDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyFileDao;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.service.CollectCodeListService;
import org.openforis.collect.service.CollectSpeciesListService;
import org.openforis.idm.model.expression.ExpressionFactory;

import java.io.File;

import static org.openforis.collect.android.viewmodelmanager.ViewModelRepository.DatabaseViewModelRepository;

/**
 * @author Daniel Wiell
 */
public class ServiceLocator {
    public static final String MODEL_DB = "collect.db";
    private static final String NODES_DB = "nodes";
    private static CollectModelManager collectModelManager;
    private static SurveyService surveyService;
    private static TaxonService taxonService;
    private static File workingDir;
    private static AndroidDatabase modelDatabase;
    private static AndroidDatabase nodeDatabase;
    private static CollectDSLContext modelDbJooqDsl;

    /**
     * Initializes the ServiceLocator.
     * Returns true if there is a selected survey, false otherwise
     */
    public static boolean init(Context applicationContext) throws WorkingDirNotWritable {
        if (surveyService == null) {
            SettingsActivity.init(applicationContext);
            UILanguageInitializer.init(applicationContext);
            workingDir = AppDirs.root(applicationContext);
            String surveyName = SurveyImporter.selectedSurvey(applicationContext);
            if (surveyName == null || !isSurveyImported(surveyName, applicationContext))
                return false;
            modelDatabase = createModelDatabase(surveyName, applicationContext);
            nodeDatabase = createNodeDatabase(surveyName, applicationContext);

            DefaultConfiguration jooqConfig = new DefaultConfiguration();
            jooqConfig.setSettings(jooqConfig.settings().withRenderSchema(false));
            jooqConfig
                    .set(modelDatabase.dataSource())
                    .set(SQLDialect.SQLITE);
            modelDbJooqDsl = new CollectDSLContext(jooqConfig);

            new ModelDatabaseMigrator(modelDatabase, surveyName, applicationContext).migrateIfNeeded();

            taxonService = createTaxonService(modelDatabase);

            initModelManager(applicationContext);
        }
        return true;
    }

    private static void initModelManager(Context applicationContext) {
        String surveyName = SurveyImporter.selectedSurvey(applicationContext);
        if (surveyName != null) {
            collectModelManager = createCollectModelManager(modelDatabase, nodeDatabase, surveyName, applicationContext);
            surveyService = createSurveyService(collectModelManager, nodeDatabase);
            surveyService.loadSurvey();
        }
    }

    public static void reset(Context context) {
        surveyService = null;
        if (modelDatabase != null)
            modelDatabase.close();
        if (nodeDatabase != null)
            nodeDatabase.close();
        init(context.getApplicationContext());
    }

    public static void resetModelManager(Context context) {
        UiNode previousSelectedNode = surveyService == null ? null : surveyService.selectedNode();
        initModelManager(context);
        if (previousSelectedNode != null) {
            UiRecord previousSelectedRecord = previousSelectedNode.getUiRecord();
            if (previousSelectedRecord != null) {
                surveyService.selectRecord(previousSelectedRecord.getId());
                surveyService.selectNode(previousSelectedNode.getId());
            }
        }
    }

    private static File databasePath(String databaseName, String surveyName, Context context) {
        return new File(AppDirs.surveyDatabasesDir(surveyName, context), databaseName);
    }

    public static boolean importSurvey(String surveyDatabasePath, boolean overwrite, Context applicationContext) throws MalformedSurvey, WrongSurveyVersion, UnsupportedFileType {
        boolean imported = new SurveyImporter(surveyDatabasePath, applicationContext).importSurvey(overwrite);
        if (imported) {
            surveyService = null;
            init(applicationContext);
        }
        return imported;
    }

    public static void importDefaultSurvey(Context context) {
        SurveyImporter.importDefaultSurvey(context);
    }

    public static void deleteNodeDatabase(Context applicationContext, String surveyName) {
        deleteDatabase(NODES_DB, surveyName, nodeDatabase, applicationContext);
    }

    public static void deleteModelDatabase(Context applicationContext, String surveyName) {
        deleteDatabase(MODEL_DB, surveyName, modelDatabase, applicationContext);
    }

    private static void deleteDatabase(String databaseName, String surveyName, AndroidDatabase database, Context applicationContext) {
        if (database != null) {
            database.close();
            File nodesDbPath = databasePath(databaseName, surveyName, applicationContext);
            nodesDbPath.delete();
        }
    }

    public static boolean isSurveyImported(String surveyName, Context context) {
        return context.getDatabasePath(databasePath(MODEL_DB, surveyName, context).getAbsolutePath()).exists();
    }

    private static AndroidDatabase createModelDatabase(String surveyName, Context applicationContext) {
        return new AndroidDatabase(applicationContext, databasePath(MODEL_DB, surveyName, applicationContext));
    }

    private static AndroidDatabase createNodeDatabase(String surveyName, Context applicationContext) {
        return new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                applicationContext,
                databasePath(NODES_DB, surveyName, applicationContext)
        );
    }

    public static SurveyService surveyService() {
        return surveyService;
    }

    public static CodeListService codeListService() {
        return collectModelManager;
    }

    public static CoordinateDestinationService coordinateDestinationService() {
        return collectModelManager;
    }

    public static TaxonService taxonService() {
        return taxonService;
    }

    private static CollectModelBackedSurveyService createSurveyService(CollectModelManager collectModelManager, Database database) {
        return new CollectModelBackedSurveyService(
                new ViewModelManager(
                        new DatabaseViewModelRepository(collectModelManager, new DataSourceNodeRepository(database))
                ),
                collectModelManager, workingDir
        );
    }

    private static CollectModelManager createCollectModelManager(AndroidDatabase modelDatabase, Database nodeDatabase, final String surveyName, final Context context) {
        DatabaseExternalCodeListProvider externalCodeListProvider = createExternalCodeListProvider(modelDatabase);

        CodeListManager codeListManager = new CodeListManager();
        MobileCodeListItemDao codeListItemDao = new MobileCodeListItemDao(modelDatabase);
        codeListItemDao.setDsl(modelDbJooqDsl);
        codeListManager.setCodeListItemDao(codeListItemDao);
        codeListManager.setExternalCodeListProvider(externalCodeListProvider);

        CollectValidator validator = new CollectValidator();
        validator.setCodeListManager(codeListManager);

        SamplingDesignDao samplingDesignDao = new SamplingDesignDao();
        samplingDesignDao.setDsl(modelDbJooqDsl);

        DynamicTableDao dynamicTableDao = new DynamicTableDao();
        dynamicTableDao.setDsl(modelDbJooqDsl);

        DatabaseLookupProvider databaseLookupProvider = new DatabaseLookupProvider();
        databaseLookupProvider.setSamplingDesignDao(samplingDesignDao);
        databaseLookupProvider.setDynamicTableDao(dynamicTableDao);

        ExpressionFactory expressionFactory = new ExpressionFactory();
        expressionFactory.setLookupProvider(databaseLookupProvider);

        CollectSurveyContext collectSurveyContext = new CollectSurveyContext(expressionFactory, validator);
        collectSurveyContext.setExternalCodeListProvider(externalCodeListProvider);
        CollectCodeListService codeListService = new CollectCodeListService();
        codeListService.setCodeListManager(codeListManager);
        collectSurveyContext.setCodeListService(codeListService);

        SpeciesManager speciesManager = createSpeciesManager(expressionFactory);
        CollectSpeciesListService speciesListService = new CollectSpeciesListService();
        speciesListService.setSpeciesManager(speciesManager);
        collectSurveyContext.setSpeciesListService(speciesListService);

        SurveyFileDao surveyFileDao = new SurveyFileDao();
        surveyFileDao.setDsl(modelDbJooqDsl);

        final CollectSurveyIdmlBinder surveySerializer = new CollectSurveyIdmlBinder(collectSurveyContext);
        SurveyDao surveyDao = new SurveyDao();
        surveyDao.setSurveySerializer(surveySerializer);
        surveyDao.setDsl(modelDbJooqDsl);
        SurveyManager surveyManager = new SurveyManager();
        surveyManager.setSurveySerializer(surveySerializer);
        surveyManager.setSurveyDao(surveyDao);
        surveyManager.setCodeListManager(codeListManager);
        surveyManager.setCollectSurveyContext(collectSurveyContext);
        surveyManager.setSurveyDao(surveyDao);
        surveyManager.setSurveyFileDao(surveyFileDao);

        RecordManager recordManager = new MobileRecordManager(
                codeListManager,
                surveyManager,
                new RecordUniquenessChecker.DataSourceRecordUniquenessChecker(nodeDatabase)
        );
        validator.setRecordManager(recordManager);

        RecordFileManager recordFileManager = new RecordFileManager() {{
            storageDirectory = AppDirs.surveyImagesDir(surveyName, context);
            if (!storageDirectory.exists()) {
                if (!storageDirectory.mkdirs())
                    throw new WorkingDirNotWritable(storageDirectory);
                AndroidFiles.makeDiscoverable(storageDirectory, context);
            }
        }};
        recordFileManager.setDefaultRootStoragePath(AppDirs.surveyDatabasesDir(surveyName, context).getAbsolutePath());
        return new CollectModelManager(surveyManager, recordManager, codeListManager, speciesManager,
                recordFileManager, modelDatabase, Settings.getPreferredLanguageMode(),
                Settings.getPreferredLanguage());
    }

    private static DatabaseExternalCodeListProvider createExternalCodeListProvider(AndroidDatabase modelDatabase) {
        DatabaseExternalCodeListProvider externalCodeListProvider = new MobileExternalCodeListProvider(modelDatabase);
        DynamicTableDao dynamicTableDao = new DynamicTableDao();
        dynamicTableDao.setDsl(modelDbJooqDsl);
        externalCodeListProvider.setDynamicTableDao(dynamicTableDao);
        return externalCodeListProvider;
    }

    private static SpeciesManager createSpeciesManager(ExpressionFactory expressionFactory) {
        SpeciesManager speciesManager = new SpeciesManager();
        TaxonDao taxonDao = new TaxonDao();
        taxonDao.setDsl(modelDbJooqDsl);
        speciesManager.setTaxonDao(taxonDao);
        TaxonomyDao taxonomyDao = new TaxonomyDao();
        taxonomyDao.setDsl(modelDbJooqDsl);
        speciesManager.setTaxonomyDao(taxonomyDao);
        TaxonVernacularNameDao taxonVernacularNameDao = new TaxonVernacularNameDao();
        taxonVernacularNameDao.setDsl(modelDbJooqDsl);
        speciesManager.setTaxonVernacularNameDao(taxonVernacularNameDao);
        speciesManager.setExpressionFactory(expressionFactory);
        return speciesManager;
    }

    private static TaxonService createTaxonService(Database modelDatabase) {
        return new TaxonRepository(modelDatabase);
    }
}
