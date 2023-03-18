package org.openforis.collect.android.gui.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyDataExportParameters;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.collectadapter.SurveyExporter;
import org.openforis.collect.android.gui.AllRecordKeysNotSpecifiedDialog;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.MimeType;
import org.openforis.collect.android.gui.util.SlowAsyncTask;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportDialogFragment extends DialogFragment {
    private static final int EXCLUDE_BINARIES = 0;
    private static final int EXCLUDE_CALCULATED_ATTRIBUTE_VALUES = 1;
    private static final int ONLY_SELECTED_RECORDS = 2;

    private Dialog dialog = null;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SurveyService surveyService = ServiceLocator.surveyService();
        UiNode selectedNode = surveyService.selectedNode();
        List<String> options = new ArrayList<String>(Arrays.asList(
                getString(R.string.export_dialog_option_exclude_binary_file),
                getString(R.string.export_dialog_option_exclude_calculated_values)
        ));
        if (!(selectedNode instanceof UiRecordCollection)) {
            options.add(getString(R.string.export_dialog_option_only_current_record));
        }
        final boolean[] selection = new boolean[options.size()];

        this.dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.export_dialog_title)
                .setMultiChoiceItems(options.toArray(new String[options.size()]), selection, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selection[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.action_export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final SurveyDataExportParameters exportParameters = new SurveyDataExportParameters();
                        exportParameters.excludeBinaries = selection[EXCLUDE_BINARIES];
                        exportParameters.excludeCalculatedAttributeValues = selection[EXCLUDE_CALCULATED_ATTRIBUTE_VALUES];
                        exportParameters.onlySelectedRecords = selection.length > ONLY_SELECTED_RECORDS && selection[ONLY_SELECTED_RECORDS];

                        Activity activity = getActivity();

                        if (ExportDialogFragment.this.dialog != null) ExportDialogFragment.this.dialog.dismiss();

                        openShareOrSaveDialog(activity, exportParameters);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        return dialog;
    }

    private static void openShareOrSaveDialog(final Activity activity, final SurveyDataExportParameters exportParameters) {
        List<String> options = new ArrayList<String>(Arrays.asList(
                activity.getString(R.string.export_dialog_option_share),
                activity.getString(R.string.export_dialog_option_save_to_downloads)
        ));
        final int[] checkedItem = {0};

        new AlertDialog.Builder(activity)
                .setTitle(R.string.export_dialog_title)
                .setSingleChoiceItems(options.toArray(new String[options.size()]), checkedItem[0], new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItem[0] = which;
                    }
                })
                .setPositiveButton(R.string.action_export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkedItem[0] == 1) {
                            exportParameters.saveToDownloads = true;
                        }
                        new ExportTask(activity, exportParameters)
                                .execute();
                    }
                })
                .create()
                .show();
    }

    private static class ExportTask extends SlowAsyncTask<Void, Void, File> {

        final SurveyDataExportParameters parameters;

        ExportTask(Activity context, SurveyDataExportParameters parameters) {
            super(context, R.string.export_progress_dialog_title, R.string.please_wait);
            this.parameters = parameters;
        }

        @Override
        protected File runTask() throws Exception {
            SurveyService surveyService = ServiceLocator.surveyService();
            parameters.filterRecordIds = parameters.onlySelectedRecords ? getSelectedRecordIds() : null;
            File exportedFile = surveyService.exportSurvey(AppDirs.surveysDir(context), parameters);
            AndroidFiles.makeDiscoverable(exportedFile, context);
            if (parameters.saveToDownloads) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                    throw new IOException("Cannot create Download folder: " + downloadDir.getAbsolutePath());
                }
                File downloadDirDestinationFile = new File(downloadDir, exportedFile.getName());
                IOUtils.copy(new FileInputStream(exportedFile), new FileOutputStream(downloadDirDestinationFile));
                AndroidFiles.makeDiscoverable(downloadDirDestinationFile, context);
            }
            return exportedFile;
        }

        public List<Integer> getSelectedRecordIds() {
            SurveyService surveyService = ServiceLocator.surveyService();
            UiNode selectedNode = surveyService.selectedNode();
            return selectedNode instanceof UiRecordCollection ? null : Arrays.asList(selectedNode.getUiRecord().getId());
        }

        @Override
        protected void onPostExecute(File exportedFile) {
            super.onPostExecute(exportedFile);
            if (exportedFile != null) {
                if (parameters.saveToDownloads) {
                    Dialogs.alert(context, R.string.export_completed_title, R.string.export_to_downloads_completed_message);
                } else {
                    Activities.shareFile(context, exportedFile, MimeType.BINARY, R.string.export_share_with_application, false);
                }
            }
        }

        @Override
        protected void handleException(Exception e) {
            super.handleException(e);

            if (e instanceof SurveyExporter.AllRecordKeysNotSpecified) {
                DialogFragment dialog = new AllRecordKeysNotSpecifiedDialog();
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "allRecordKeysNotSpecifiedDialog");
            } else {
                String surveyName = ServiceLocator.surveyService().getSelectedSurvey().getName();
                String message = context.getString(R.string.survey_export_failed_message, surveyName, e.getMessage());
                Log.e("export", message, e);
                Dialogs.alert(context, context.getString(R.string.survey_export_failed_title), message);
            }
        }
    }
}