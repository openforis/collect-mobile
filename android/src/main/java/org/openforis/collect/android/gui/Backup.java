package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.StorageLocations;
import org.openforis.collect.android.sqlite.AndroidDatabase;

import java.io.File;
import java.io.IOException;

public class Backup {

    private FragmentActivity activity;

    public Backup(FragmentActivity activity) {
        this.activity = activity;
    }

    public void execute() {
        try {
            backupToTemp();

            if (StorageLocations.usesSecondaryStorage(activity))
                showInsertSdCardDialog(activity);
            else
                backupFromTempToWorkingDir(activity);
        } catch (IOException e) {
            // TODO: show dialog or at least toast, indicating backup failed
        }
    }

    private static void showInsertSdCardDialog(FragmentActivity activity) {
        Intent intent = new Intent();
        intent.setAction(AndroidDatabase.ACTION_PREPARE_EJECT);
        activity.sendBroadcast(intent);
        DialogFragment dialogFragment = new BackupDialogFragment();
        dialogFragment.show(activity.getSupportFragmentManager(), "backupInsertSdCard");
    }

    private void backupToTemp() throws IOException {
        File workingDir = ServiceLocator.workingDir(activity);
        File tempDir = tempDir(activity);
        if (tempDir.exists())
            FileUtils.deleteDirectory(tempDir);
        FileUtils.copyDirectory(workingDir, tempDir);
    }

    private static File tempDir(FragmentActivity activity) {
        File workingDir = ServiceLocator.workingDir(activity);
        return new File(activity.getExternalCacheDir(), workingDir.getName());
    }

    private static void backupFromTempToWorkingDir(FragmentActivity activity) throws IOException {
        File tempDir = tempDir(activity);
        File workingDir = ServiceLocator.workingDir(activity);
        if (workingDir.exists()) {
            File timestampedWorkingDir = new File(workingDir.getParentFile(), workingDir.getName() + "-" + System.currentTimeMillis());
            FileUtils.moveDirectory(workingDir, timestampedWorkingDir);
        }
        FileUtils.moveDirectory(tempDir, workingDir);
    }

    public static class BackupDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.backup_insert_sd_card)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                FileUtils.deleteDirectory(tempDir(getActivity()));
                            } catch (IOException ignore) {
                            }
                        }
                    })
                    .create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                backupFromTempToWorkingDir(getActivity());
                                alertDialog.dismiss();
                            } catch (Exception ignore) {

                            }
                        }
                    });
                }
            });
            return alertDialog;
        }

    }

}
