package org.openforis.collect.android.gui.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.SurveyExporter;
import org.openforis.collect.android.gui.AllRecordKeysNotSpecifiedDialog;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.SlowAsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportDialogFragment extends DialogFragment {
    private static final int EXCLUDE_BINARIES = 0;
    private static final int SAVE_TO_DOWNLOADS = 1;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CharSequence[] options = {
                getContext().getString(R.string.export_dialog_option_exclude_binary_file),
                getContext().getString(R.string.export_dialog_option_save_to_downloads),
        };
        final boolean[] selection = new boolean[options.length];
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.export_dialog_title)
                .setMultiChoiceItems(options, selection, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selection[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.action_export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new ExportTask(getActivity(), selection[SAVE_TO_DOWNLOADS], selection[EXCLUDE_BINARIES])
                                .execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private static class ExportTask extends SlowAsyncTask<Void, Void, File> {

        final boolean saveToDownloads;
        final boolean excludeBinaries;

        ExportTask(Activity context, boolean saveToDownloads, boolean excludeBinaries) {
            super(context, R.string.export_progress_dialog_title, R.string.please_wait);
            this.saveToDownloads = saveToDownloads;
            this.excludeBinaries = excludeBinaries;
        }

        @Override
        protected File runTask() throws Exception {
            File exportedFile = ServiceLocator.surveyService().exportSurvey(excludeBinaries);
            AndroidFiles.makeDiscoverable(exportedFile, context);
            if (saveToDownloads) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) {
                    if (!downloadDir.mkdirs()) {
                        throw new IOException("Cannot create Download folder: " + downloadDir.getAbsolutePath());
                    }
                }
                File downloadDirDestinationFile = new File(downloadDir, exportedFile.getName());
                IOUtils.copy(new FileInputStream(exportedFile), new FileOutputStream(downloadDirDestinationFile));
                AndroidFiles.makeDiscoverable(downloadDirDestinationFile, context);
            }
            return exportedFile;
        }

        @Override
        protected void onPostExecute(File exportedFile) {
            super.onPostExecute(exportedFile);
            if (exportedFile != null) {
                if (saveToDownloads) {
                    Dialogs.alert(context, R.string.export_completed_title, R.string.export_to_downloads_completed_message);
                } else {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportedFile));
                    shareIntent.setType("*/*");
                    context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.export_share_with_application)));
                }
            }
        }

        @Override
        protected void handleException(Exception e) {
            super.handleException(e);

            if (e instanceof IOException) {
                String message = context.getString(R.string.toast_exported_survey_failed, e.getMessage());
                Log.e("export", message, e);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } else if (e instanceof SurveyExporter.AllRecordKeysNotSpecified) {
                DialogFragment dialog = new AllRecordKeysNotSpecifiedDialog();
                dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "allRecordKeysNotSpecifiedDialog");
            }
        }
    }
}