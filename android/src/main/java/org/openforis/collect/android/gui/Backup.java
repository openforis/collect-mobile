package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.sqlite.AndroidDatabase;

import java.io.File;
import java.io.IOException;

public class Backup {

    public static void showBackupModeChooseDialog(FragmentActivity activity) {
        DialogFragment dialogFragment = new BackupModeDialogFragment();
        dialogFragment.show(activity.getSupportFragmentManager(), "backupModeSelection");
    }

    private static class BackupExecutor {
        private FragmentActivity activity;
        private File tempDir;
        private File surveysDir;

        public BackupExecutor(FragmentActivity activity) {
            this.activity = activity;

            surveysDir = AppDirs.surveysDir(activity);
            tempDir = new File(activity.getExternalCacheDir(), surveysDir.getName());
        }

        public void backupToNewSdCard() {
            try {
                if (backupToTemp()) {
                    showInsertSdCardDialog();
                }
            } catch (IOException e) {
                showBackupErrorMessage(e);
            }
        }

        public void onNewSdCardInsertConfirm() {
            try {
                if (copyBackupFromTempToSurveysDir()) {
                    showBackupCompleteMessage();
                }
            } catch (Exception e) {
                showBackupErrorMessage(e);
            }
        }

        public void cancelBackupToNewSdCard() {
            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException ignore) {
            }
        }

        public void backupInternally() {
            File snapshotSurveysDir = getNewSnapshotSurveysDir();
            if (AndroidFiles.enoughSpaceToCopy(surveysDir, snapshotSurveysDir)) {
                try {
                    FileUtils.copyDirectory(surveysDir, snapshotSurveysDir);
                    AndroidFiles.makeDiscoverable(snapshotSurveysDir, activity);
                    showBackupCompleteMessage();
                } catch (IOException e) {
                    showBackupErrorMessage(e);
                }
            } else {
                Dialogs.alert(activity, R.string.warning, R.string.backup_not_enough_space_working_directory);
            }
        }

        private void showInsertSdCardDialog() {
            Intent intent = new Intent();
            intent.setAction(AndroidDatabase.ACTION_PREPARE_EJECT);
            activity.sendBroadcast(intent);
            DialogFragment dialogFragment = new BackupDialogFragment();
            dialogFragment.show(activity.getSupportFragmentManager(), "backupInsertSdCard");
        }

        private boolean backupToTemp() throws IOException {
            if (tempDir.exists())
                FileUtils.deleteDirectory(tempDir);

            if (AndroidFiles.enoughSpaceToCopy(surveysDir, tempDir)) {
                FileUtils.copyDirectory(surveysDir, tempDir);
                return true;
            } else {
                Dialogs.alert(activity, R.string.warning, R.string.backup_not_enough_space_internal);
                return false;
            }
        }

        private boolean copyBackupFromTempToSurveysDir() throws IOException {
            if (AndroidFiles.enoughSpaceToCopy(tempDir, surveysDir)) {
                if (surveysDir.exists()) {
                    File snapshotSurveysDir = getNewSnapshotSurveysDir();
                    FileUtils.moveDirectory(surveysDir, snapshotSurveysDir);
                    AndroidFiles.makeDiscoverable(snapshotSurveysDir, activity);
                }
                FileUtils.moveDirectory(tempDir, surveysDir);
                AndroidFiles.makeDiscoverable(surveysDir, activity);
                return true;
            } else {
                Dialogs.alert(activity, R.string.warning, R.string.backup_not_enough_space_working_directory);
                return false;
            }
        }

        private File getNewSnapshotSurveysDir() {
            return new File(surveysDir.getParentFile(), surveysDir.getName() + "-" + System.currentTimeMillis());
        }

        private void showBackupCompleteMessage() {
            String message = activity.getString(R.string.backup_complete, AppDirs.root(activity));
            Dialogs.info(activity, R.string.info, message);
        }

        private void showBackupErrorMessage(Exception e) {
            String message = activity.getResources().getString(R.string.backup_failed, e.getMessage());
            Log.e("Backup", message, e);
            Dialogs.alert(activity, R.string.warning, message);
        }
    }

    public static class BackupModeDialogFragment extends DialogFragment {
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.backup_mode_choose)
                    .setSingleChoiceItems(R.array.backup_modes, 0, null)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            BackupExecutor backupExecutor = new BackupExecutor(getActivity());
                            ListView lw = alertDialog.getListView();
                            switch (lw.getCheckedItemPosition()) {
                                case 0:
                                    backupExecutor.backupToNewSdCard();
                                    break;
                                case 1:
                                    backupExecutor.backupInternally();
                                    break;
                            }
                            alertDialog.dismiss();
                        }
                    });
                }
            });
            return alertDialog;
        }
    }

    public static class BackupDialogFragment extends DialogFragment {
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final BackupExecutor backupExecutor = new BackupExecutor(getActivity());
            final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.backup_insert_sd_card)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            backupExecutor.cancelBackupToNewSdCard();
                        }
                    })
                    .create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            backupExecutor.onNewSdCardInsertConfirm();
                            alertDialog.dismiss();
                        }
                    });
                }
            });
            return alertDialog;
        }
    }


}
