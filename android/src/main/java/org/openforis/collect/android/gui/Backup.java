package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AppDirs;
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
            showInsertSdCardDialog(activity);
        } catch (IOException e) {
            String message = activity.getResources().getString(R.string.toast_backed_up_survey);
            Log.e("Backup", message, e);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

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
        File databases = AppDirs.databases(activity);
        File tempDir = tempDir(activity);
        if (tempDir.exists())
            FileUtils.deleteDirectory(tempDir);
        FileUtils.copyDirectory(databases, tempDir);
    }

    private static File tempDir(FragmentActivity activity) {
        File databases = AppDirs.databases(activity);
        return new File(activity.getExternalCacheDir(), databases.getName());
    }

    private static void backupFromTempToTargetDir(FragmentActivity activity, File targetDir) throws IOException {
        File tempDir = tempDir(activity);
        if (targetDir.exists()) {
            File timestampedDatabases = new File(targetDir.getParentFile(), targetDir.getName() + "-" + System.currentTimeMillis());
            FileUtils.moveDirectory(targetDir, timestampedDatabases);
            makeDiscoverable(activity, timestampedDatabases);
        }
        FileUtils.moveDirectory(tempDir, targetDir);
        makeDiscoverable(activity, targetDir);
    }

    private static void makeDiscoverable(Context context, File targetDir) {
        MediaScannerConnection.scanFile(context, new String[]{targetDir.getAbsolutePath()}, null, null);
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
                                backupFromTempToTargetDir(getActivity(), AppDirs.databases(getActivity()));
                                alertDialog.dismiss();
                                String message = getResources().getString(R.string.toast_backed_up_survey, AppDirs.root(getActivity()));
                                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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
