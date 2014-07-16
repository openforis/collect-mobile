package org.openforis.collect.android.gui;

import android.content.Context;
import android.os.Environment;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.*;
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog;
import org.openforis.collect.android.gui.util.StorageLocations;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.NodeSchemaChangeLog;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodelmanager.DataSourceNodeRepository;
import org.openforis.collect.android.viewmodelmanager.TaxonService;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.persistence.DynamicTableDao;

import java.io.File;

import static org.openforis.collect.android.gui.util.StorageLocations.usesSecondaryStorage;
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

    public static boolean init(Context applicationContext) {
        if (surveyService == null) {
            workingDir = workingDir(applicationContext);
            if (!isSurveyImported(applicationContext))
                return false;
            AndroidDatabase modelDatabase = new AndroidDatabase(applicationContext, databasePath(MODEL_DB));
            AndroidDatabase nodeDatabase = createNodeDatabase(applicationContext);
            collectModelManager = createCollectModelManager(modelDatabase, nodeDatabase);
            SurveyService surveyService = createSurveyService(collectModelManager, nodeDatabase);
            surveyService.loadSurvey();
            taxonService = createTaxonService(modelDatabase);
            ServiceLocator.surveyService = surveyService;
        }
        return true;
    }

    private static File databasePath(String databaseName) {
        return new File(workingDir, databaseName);
    }

    public static File workingDir(Context applicationContext) {
        File workingDir = null;
        if (usesSecondaryStorage(applicationContext)) {
            if (!StorageLocations.isSecondaryStorageWritable())
                throw new StorageNotAvailableException();
            workingDir = secondaryStorageWorkingDir();
        }
        if (workingDir == null)
            workingDir = applicationContext.getDatabasePath("database").getParentFile();
        return workingDir;
    }

    private static File secondaryStorageWorkingDir() {
        File secondaryStorageLocation = StorageLocations.secondaryStorageLocation();
        File workingDir = new File(secondaryStorageLocation, "org.openforis.collect");
        if (!workingDir.exists())
            if (!workingDir.mkdir())
                throw new StorageNotAvailableException();
        return workingDir;
    }

    public static void importSurvey(String surveyDatabasePath, Context applicationContext) {
        new SurveyImporter(surveyDatabasePath, applicationContext, databasePath(MODEL_DB)).importSurvey();
    }

    public static boolean isSurveyImported(Context context) {
        return context.getDatabasePath(databasePath(MODEL_DB).getAbsolutePath()).exists();
    }

    private static AndroidDatabase createNodeDatabase(Context applicationContext) {
        return new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                applicationContext,
                databasePath(NODES_DB)
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

    private static File collectDir() {
        return StorageLocations.isSecondaryStorageWritable()
                ? StorageLocations.secondaryStorageLocation()
                : Environment.getExternalStorageDirectory();
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
        return new File(collectDir(), "survey_export.zip");
    }

    private static CollectModelManager createCollectModelManager(AndroidDatabase modelDatabase, Database nodeDatabase) {
        DatabaseExternalCodeListProvider externalCodeListProvider = createExternalCodeListProvider(modelDatabase);
        CodeListManager codeListManager = new MeteredCodeListManager(new MobileCodeListItemDao(modelDatabase),
                externalCodeListProvider
        );

        MeteredValidator validator = new MeteredValidator(codeListManager);
        SurveyManager surveyManager = new MeteredSurveyManager(codeListManager, validator, externalCodeListProvider, modelDatabase);

        RecordManager recordManager = new MobileRecordManager(
                codeListManager,
                surveyManager,
                new RecordUniquenessChecker.DataSourceRecordUniquenessChecker(nodeDatabase)
        );

        RecordManager meteredRecordManager = new MeteredRecordManager(recordManager);
        validator.setRecordManager(meteredRecordManager);

        return new CollectModelManager(surveyManager, meteredRecordManager, codeListManager, modelDatabase);
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
