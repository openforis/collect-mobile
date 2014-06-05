package org.openforis.collect.android.gui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import liquibase.database.core.AndroidSQLiteDatabase;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.*;
import org.openforis.collect.android.databaseschema.ModelDatabaseSchemaUpdater;
import org.openforis.collect.android.databaseschema.NodeDatabaseSchemaChangeLog;
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
import java.io.IOException;

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

    public static boolean init(Context applicationContext) {
        if (surveyService == null) {
            if (!isSurveyImported(applicationContext))
                return false;
            AndroidDatabase modelDatabase = new AndroidDatabase(applicationContext, MODEL_DB);
            AndroidDatabase nodeDatabase = createNodeDatabase(applicationContext);
            collectModelManager = createCollectModelManager(modelDatabase, nodeDatabase);
            SurveyService surveyService = createSurveyService(collectModelManager, nodeDatabase);
            surveyService.loadSurvey();
            taxonService = createTaxonService(modelDatabase);
            ServiceLocator.surveyService = surveyService;
        }
        return true;
    }

    public static void importSurvey(String surveyDatabasePath, Context applicationContext) {
        try {
            verifyDatabase(surveyDatabasePath);
            applicationContext.openOrCreateDatabase(MODEL_DB, 0, null);
            File targetSurveyDatabase = applicationContext.getDatabasePath(MODEL_DB);
            FileUtils.copyFile(new File(surveyDatabasePath), targetSurveyDatabase);
        } catch (IOException e) {
            Toast.makeText(applicationContext, "Failed to load survey.", Toast.LENGTH_SHORT).show();
            Log.w(ServiceLocator.class.getSimpleName(), "Failed to load survey", e);
        } catch (SQLiteException e) {
            Toast.makeText(applicationContext, "Failed to load survey.", Toast.LENGTH_SHORT).show();
            Log.w(ServiceLocator.class.getSimpleName(), "Failed to load survey", e);
        }
    }

    /**
     * Reality check on the database file. It needs to be an openable SQLite database file,
     * and contain the ofc_survey table
     */
    private static void verifyDatabase(String surveyDatabasePath) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(surveyDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);
        db.rawQuery("select * from ofc_survey", new String[0]);
    }

    public static boolean isSurveyImported(Context context) {
        return context.getDatabasePath(MODEL_DB).exists();
    }

    private static AndroidDatabase createNodeDatabase(Context applicationContext) {
        return new AndroidDatabase(
                new NodeSchemaChangeLog(
                        new NodeDatabaseSchemaChangeLog().changes()
                ),
                applicationContext,
                NODES_DB
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
        File storageDir = Environment.getExternalStorageDirectory();
        return new File(storageDir, "Collect");
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
}
