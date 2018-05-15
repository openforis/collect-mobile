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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.gui.util.SlowAsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class SurveyListActivity extends BaseActivity {

    private static final int IMPORT_SURVEY_REQUEST_CODE = 6384;
    private static final String OPEN_IMPORT_DIALOG = "openImportDialog";

    public static void startActivityAndShowImportDialog(Activity context) {
        Bundle extras = new Bundle();
        extras.putBoolean(OPEN_IMPORT_DIALOG, true);
        Activities.start(context, SurveyListActivity.class, extras);
    }

    public static void startActivity(Activity context) {
        Activities.start(context, SurveyListActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SurveyListAdapter adapter = new SurveyListAdapter(this);
        showSurveyList(adapter);

        if (Activities.getIntentExtra(this, OPEN_IMPORT_DIALOG, false)) {
            showImportDialog(this);
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
        showImportDialog(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMPORT_SURVEY_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    String path = getFilePathByUri(data.getData());
                    importSurvey(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFilePathByUri(Uri uri) {
        String path = getExistingLocalFilePathFromUri(uri);
        if (path != null)
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
        }
        throw new IllegalStateException(String.format("Failed to import survey; could not determine file path for URI: %s", uri));
    }

    @Nullable
    private String getExistingLocalFilePathFromUri(Uri uri) {
        try {
            String path = FileUtils.getPath(this, uri);
            return path != null && new File(path).exists() ? path : null;
        } catch(Exception e) {
            return null;
        }
    }

    protected static void showImportDialog(Activity context) {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(
                target, "Select survey to import");
        context.startActivityForResult(intent, IMPORT_SURVEY_REQUEST_CODE);
    }

    protected void importSurvey(String surveyPath) {
        new ImportSurveyTask(this, surveyPath).execute();
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

    private static class ImportSurveyTask extends SlowAsyncTask<Void, Void, Boolean> {

        private String surveyPath;
        private boolean overwrite = false;

        ImportSurveyTask(Activity context, String surveyPath) {
            this(context, surveyPath, false);
        }

        ImportSurveyTask(Activity context, String surveyPath, boolean overwrite) {
            super(context, null, R.string.toast_import_survey, R.string.please_wait);
            this.surveyPath = surveyPath;
            this.overwrite = overwrite;
        }

        @Override
        protected Boolean runTask() throws Exception {
            super.runTask();
            if (ServiceLocator.importSurvey(surveyPath, overwrite, context) || overwrite) {
                onSurveyImportComplete();
                return false; //survey imported successfully
            } else {
                return true; //survey already existing
            }
        }

        @Override
        protected void onPostExecute(Boolean surveyAlreadyExisting) {
            super.onPostExecute(surveyAlreadyExisting);
            if (surveyAlreadyExisting != null && surveyAlreadyExisting) {
                Dialogs.confirm(context, R.string.import_overwrite_data_dialog_title, R.string.import_overwrite_data_dialog_message,
                        new Runnable() {
                            public void run() {
                                new ImportSurveyTask(context, surveyPath, true).execute();
                            }
                        }, null);
            }
        }

        @Override
        protected void handleException(Exception e) {
            super.handleException(e);
            String errorMessage;
            if (e instanceof UnsupportedFileType) {
                UnsupportedFileType ex = (UnsupportedFileType) e;
                errorMessage = context.getString(R.string.import_text_unsupported_file_type_selected,
                        ex.getExpectedExtention());
            } else if (e instanceof MalformedSurvey) {
                errorMessage = context.getString(R.string.import_text_failed);
            } else if (e instanceof WrongSurveyVersion) {
                WrongSurveyVersion ex = (WrongSurveyVersion) e;
                errorMessage = context.getString(R.string.import_text_wrong_version,
                        ex.getSurveyVersion(), ex.getCollectVersion());
            } else {
                errorMessage = context.getString(R.string.import_text_failed);
            }
            showImportFailedDialog(context,
                    surveyPath,
                    errorMessage
            );
        }

        private void onSurveyImportComplete() {
            SurveyNodeActivity.startClearSurveyNodeActivity(context);
        }

        private void showImportFailedDialog(final Activity context, final String surveyPath, final String message) {
            new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(context.getString(R.string.import_title_failed, surveyPath))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showImportDialog(context);
                    }
                })
                .show();
        }
    }
}
