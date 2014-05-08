package org.openforis.collect.android.gui;

import android.content.Context;
import android.os.Environment;
import liquibase.database.core.AndroidSQLiteDatabase;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyException;
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

import java.io.*;

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
            boolean initialized = setupDatabases(applicationContext);
            AndroidDatabase modelDatabase = new AndroidDatabase(applicationContext, "model");
            if (!initialized)
                new ModelDatabaseSchemaUpdater().update(modelDatabase, new AndroidSQLiteDatabase());

            AndroidDatabase nodeDatabase = createNodeDatabase(applicationContext);

            collectModelManager = createCollectModelManager(modelDatabase, nodeDatabase);
            surveyService = createSurveyService(collectModelManager, nodeDatabase);
            loadOrImportSurvey(surveyService);
            taxonService = createTaxonService(modelDatabase);
        }
    }

    private static AndroidDatabase createNodeDatabase(Context applicationContext) {
        return new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                applicationContext,
                "nodes"
        );
    }

    private static boolean setupDatabases(Context context) {
        File model = context.getDatabasePath("model");
        boolean initialized = true;
        if (!model.exists())
            initialized = importOrSetupModelDatabase(context);

        File nodes = context.getDatabasePath("nodes");
        if (!nodes.exists())
            importNodesDatabase(context);
        return initialized;
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
        UiSurvey uiSurvey = surveyService.loadSurvey();
        if (uiSurvey == null)
            surveyService.importSurvey(idmXmlStream());
    }

    private static void importNodesDatabase(Context context) {
        File nodes = new File(collectDir(), "nodes");
        if (nodes.exists())
            try {
                FileUtils.copyFile(nodes, context.getDatabasePath("nodes"));
            } catch (IOException e) {
                throw new SurveyException(e);
            }
    }

    private static boolean importOrSetupModelDatabase(Context context) {
        File model = new File(collectDir(), "model");
        if (model.exists()) {
            try {
                FileUtils.copyFile(model, context.getDatabasePath("model"));
            } catch (IOException e) {
                throw new SurveyException(e);
            }
            return true;
        }
        return false;
    }

    private static File collectDir() {
        File storageDir = Environment.getExternalStorageDirectory();
        return new File(storageDir, "Collect");
    }


    public static CollectModelBackedSurveyService createSurveyService(CollectModelManager collectModelManager, Database database) {
        return new CollectModelBackedSurveyService(
                new ViewModelManager(
                        new DatabaseViewModelRepository(collectModelManager, new DataSourceNodeRepository(database))
                ),
                collectModelManager
        );
    }

    private static CollectModelManager createCollectModelManager(AndroidDatabase modelDatabase, Database nodeDatabase) {
        DatabaseExternalCodeListProvider externalCodeListProvider = createExternalCodeListProvider(modelDatabase);
        CodeListManager codeListManager = new MeteredCodeListManager(new MobileCodeListItemDao(modelDatabase),
                externalCodeListProvider
        );

        MeteredValidator validator = new MeteredValidator(codeListManager);
        SurveyManager surveyManager = new MeteredSurveyManager(codeListManager, validator, externalCodeListProvider, modelDatabase);
        RecordManager recordManager = new MeteredRecordManager(codeListManager, surveyManager);
        recordManager.setRecordDao(new MobileRecordDao(nodeDatabase));
        validator.setRecordManager(recordManager);

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
        File collectDir = collectDir();
        if (!collectDir.exists())
            if (!collectDir.mkdir())
                throw new IllegalStateException("Failed to create dir: " + collectDir);
        return new File(collectDir, "idm.xml");
    }
}
