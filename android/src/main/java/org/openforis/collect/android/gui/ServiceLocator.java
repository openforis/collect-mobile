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
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.collect.persistence.DynamicTableDao;

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
            modelDatabase = new AndroidDatabase(applicationContext, databasePath(MODEL_DB, applicationContext));
            nodeDatabase = createNodeDatabase(applicationContext);
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

    public static void importSurvey(String surveyDatabasePath, Context applicationContext) {
        new SurveyImporter(surveyDatabasePath, applicationContext, databasePath(MODEL_DB, applicationContext)).importSurvey();
    }

    public static boolean isSurveyImported(Context context) {
        return context.getDatabasePath(databasePath(MODEL_DB, context).getAbsolutePath()).exists();
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
