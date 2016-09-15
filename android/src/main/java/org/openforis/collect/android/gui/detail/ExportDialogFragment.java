package org.openforis.collect.android.gui.detail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.SurveyExporter;
import org.openforis.collect.android.gui.AllRecordKeysNotSpecifiedDialog;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.AndroidFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportDialogFragment extends DialogFragment {
    private static final int EXCLUDE_BINARIES = 0;
    private static final int SAVE_TO_DOWNLOADS = 1;

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
                        export(selection);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void export(boolean[] selection) {
        try {
            File exportedFile = ServiceLocator.surveyService().exportSurvey(selection[EXCLUDE_BINARIES]);
            AndroidFiles.makeDiscoverable(exportedFile, getActivity());

            if (selection[SAVE_TO_DOWNLOADS]) {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                IOUtils.copy(new FileInputStream(exportedFile), new FileOutputStream(new File(downloadDir, exportedFile.getName())));
            } else {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportedFile));
                shareIntent.setType("*/*");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.export_share_with_application)));
            }
        } catch (IOException e) {
            String message = getResources().getString(R.string.toast_exported_survey_failed);
            Log.e("export", message, e);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        } catch (SurveyExporter.AllRecordKeysNotSpecified e) {
            DialogFragment dialog = new AllRecordKeysNotSpecifiedDialog();
            dialog.show(getActivity().getSupportFragmentManager(), "allRecordKeysNotSpecifiedDialog");
        }
    }
}