package org.openforis.collect.android.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.ipaulpro.afilechooser.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Keyboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class SurveyListActivity extends BaseActivity {

    private static final int IMPORT_SURVEY_REQUEST_CODE = 6384;
    private static final String OPEN_IMPORT_DIALOG = "openImportDialog";

    private boolean showOverwriteDialog;
    private String surveyPath;

    public static void showImportDialog(Activity context) {
        Bundle extras = new Bundle();
        extras.putBoolean(OPEN_IMPORT_DIALOG, true);
        Activities.startActivity(context, SurveyListActivity.class, extras);
    }

    public static void startActivity(Activity context) {
        Activities.startActivity(context, SurveyListActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SurveyListAdapter adapter = new SurveyListAdapter(this);
        showSurveyList(adapter);

        Boolean openImportDialog = Activities.getExtra(this, OPEN_IMPORT_DIALOG);
        if (Boolean.TRUE.equals(openImportDialog)) {
            showImportDialog();
        } else if (adapter.isEmpty()) {
            showDemoSurveyDialog();
        }
    }

    private void showSurveyList(final SurveyListAdapter adapter) {
        setContentView(R.layout.survey_list);
        ListView listView = (ListView) findViewById(R.id.survey_list);
        final Activity activity = this;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String surveyName = adapter.survey(position);
                SurveyImporter.selectSurvey(surveyName, activity);
                SurveyNodeActivity.restartActivity(activity);
            }
        });
        adapter.setSurveysDeletedListener(new SurveyListAdapter.SurveysDeletedListener() {
            public void onSurveysDeleted(Set<String> surveys) {
                deleteSurveys(surveys);
                clearBackstack();
            }
        });
        selectSurvey(listView, adapter);
    }

    private void showDemoSurveyDialog() {
        DialogFragment newFragment = new ImportingDemoSurveyDialog();
        newFragment.show(getSupportFragmentManager(), "importingDemoSurvey");
    }

    private void deleteSurveys(Set<String> surveys) {
        File surveysDir = AppDirs.surveysDir(this);
        for (String survey : surveys) {
            File surveyDir = new File(surveysDir, survey);
            if (surveyDir.exists())
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(surveyDir);
                } catch (IOException e) {
                    Log.e(SurveyListActivity.class.getName(), "Failed to delete survey " + surveyDir, e);
                }
        }
    }

    private void clearBackstack() {
        Keyboard.hide(this);
        Intent intent = new Intent(this, SurveyListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.survey_list_activity_actions, menu);
        return true;
    }

    public void surveyImportRequested(MenuItem item) {
        showImportDialog();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMPORT_SURVEY_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    String path = getFileNameByUri(data.getData());
                    importSurvey(path, false);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private String getFileNameByUri(Uri uri) {
        String path = FileUtils.getPath(this, uri);
        if (path != null && new File(path).exists())
            return path;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                File file = new File(downloadDir, name);
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream == null)
                        throw new IllegalStateException("Failed to import survey");
                    IOUtils.copy(inputStream, new FileOutputStream(file));
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cursor.close();
                return file.getAbsolutePath();
            }
        } else if (uri.getScheme().equals("file"))
            return FileUtils.getPath(this, uri);
        throw new IllegalStateException("Failed to import survey");
    }

    protected void showImportDialog() {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(
                target, "Select survey to import");
        startActivityForResult(intent, IMPORT_SURVEY_REQUEST_CODE);
    }

    protected void importSurvey(String surveyPath, boolean overwrite) {
        String message = getResources().getString(R.string.toast_import_survey);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        try {
            if (ServiceLocator.importSurvey(surveyPath, overwrite, this) || overwrite)
                startImportedSurveyNodeActivity();
            else {
                showOverwriteDialog = true;
                this.surveyPath = surveyPath;
            }
        } catch (MalformedSurvey malformedSurvey) {
            importFailedDialog(
                    malformedSurvey.sourceName,
                    getString(R.string.import_text_failed)
            );
        } catch (WrongSurveyVersion wrongSurveyVersion) {
            importFailedDialog(
                    wrongSurveyVersion.sourceName,
                    getString(R.string.import_text_wrong_version)
            );
        }
    }

    protected void onResumeFragments() {
        super.onResumeFragments();
        if (showOverwriteDialog)
            showImportOwerwriteDialog(surveyPath);
        showOverwriteDialog = false;
        surveyPath = null;
    }

    private void showImportOwerwriteDialog(String surveyPath) {
        ImportOverwriteDataConfirmation dialog = ImportOverwriteDataConfirmation.create(surveyPath);
        dialog.show(getSupportFragmentManager(), "confirmDataDeletionAndImport");
    }

    public void startImportedSurveyNodeActivity() {
        Intent intent = new Intent(this, SurveyNodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void importFailedDialog(String surveyPath, String message) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.import_title_failed, surveyPath))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showImportDialog();
                    }
                })
                .show();
    }

    private void selectSurvey(ListView listView, SurveyListAdapter adapter) {
        String selectedSurvey = SurveyImporter.selectedSurvey(this);
        if (selectedSurvey != null) {
            int position = adapter.position(selectedSurvey);
            if (position >= 0) {
                listView.setSelection(position);
                listView.setItemChecked(position, true);
            }
        }
    }
}
