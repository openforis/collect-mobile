package org.openforis.collect.android.gui.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.BackupGenerator;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.MimeType;
import org.openforis.collect.android.sqlite.AndroidDatabase;

import java.io.File;
import java.io.IOException;

public class Backup {

    private static String BACKUP_FILE_PREFIX = "collect_mobile_backup_";
    public static String BACKUP_FILE_EXTENSION = "ofcmbck";

    public static void showBackupModeChooseDialog(AppCompatActivity activity) {
        DialogFragment dialogFragment = new BackupModeDialogFragment();
        dialogFragment.show(activity.getSupportFragmentManager(), "backupModeSelection");
    }

    private static class BackupExecutor {
        private FragmentActivity context;
        private File tempDir;
        private File surveysDir;
        private SnapshotsManager snapshotsManager;

        public BackupExecutor(FragmentActivity context) {
            this.context = context;

            surveysDir = AppDirs.surveysDir(context);
            tempDir = new File(context.getExternalCacheDir(), surveysDir.getName());
            snapshotsManager = new SnapshotsManager(surveysDir);
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
            File snapshotSurveysDir = snapshotsManager.newSnapshotDir();
            if (AndroidFiles.enoughSpaceToCopy(surveysDir, snapshotSurveysDir)) {
                // perform the backup process (create new snapshot)
                try {
                    FileUtils.copyDirectory(surveysDir, snapshotSurveysDir);
                    AndroidFiles.makeDiscoverable(snapshotSurveysDir, context);
                    showBackupCompleteMessage();
                } catch (IOException e) {
                    showBackupErrorMessage(e);
                }
            } else if (snapshotsManager.existsSnapshot()) {
                // Try to delete the oldest snapshot before the backup process
                String oldestSnapshotDateFormatted = snapshotsManager.getOldestSnapshotDateFormatted();
                String message = context.getString(R.string.backup_not_enough_space_working_directory) +
                        context.getString(R.string.backup_delete_oldest_snapshot, oldestSnapshotDateFormatted);
                Dialogs.confirm(context, R.string.confirm_label, message, new Runnable() {
                    public void run() {
                        try {
                            snapshotsManager.deleteOldestSnapshot();
                            // try again the backup process
                            backupInternally();
                        } catch (IOException e) {
                            String message = context.getString(R.string.backup_delete_oldest_snapshot_failed, e.getMessage());
                            Dialogs.alert(context, R.string.warning, message);
                        }
                    }
                });
            } else {
                Dialogs.alert(context, R.string.warning, R.string.backup_not_enough_space_internal);
            }
        }

        private void backupIntoDownloads() {
            try {
                File downloadDir = AndroidFiles.getDownloadsDir(context);
                File backupFile = generateBackupFile();
                if (backupFile == null) return;
                File downloadDirDestinationFile = new File(downloadDir, backupFile.getName());
                FileUtils.copyFile(backupFile, downloadDirDestinationFile);
                AndroidFiles.makeDiscoverable(downloadDirDestinationFile, context);
                Dialogs.alert(context, R.string.backup_file_generation_complete, R.string.backup_file_generation_into_downloads_complete_message);
            } catch (Exception e) {
                showBackupErrorMessage(e);
            }
        }

        private void backupAndShare() {
            File backupFile = generateBackupFile();
            if (backupFile == null) return;
            Activities.shareFile(context, backupFile, MimeType.BINARY, R.string.share_file, false);
        }

        private File generateBackupFile() {
            File destFile = null;
            try {
                destFile = File.createTempFile(BACKUP_FILE_PREFIX, "." + BACKUP_FILE_EXTENSION);
                if (AndroidFiles.enoughSpaceToCopy(surveysDir, destFile.getParentFile())) {
                    BackupGenerator backupGenerator = new BackupGenerator(surveysDir, App.versionName(context), destFile);
                    backupGenerator.generate();
                } else {
                    destFile.delete();
                }
            } catch (IOException e) {
                showBackupErrorMessage(e);
                if (destFile != null) {
                    destFile.delete();
                }
            }
            return destFile;
        }

        private void showInsertSdCardDialog() {
            Intent intent = new Intent();
            intent.setAction(AndroidDatabase.ACTION_PREPARE_EJECT);
            context.sendBroadcast(intent);
            DialogFragment dialogFragment = new BackupDialogFragment();
            dialogFragment.show(context.getSupportFragmentManager(), "backupInsertSdCard");
        }

        private boolean backupToTemp() throws IOException {
            if (tempDir.exists())
                FileUtils.deleteDirectory(tempDir);

            if (AndroidFiles.enoughSpaceToCopy(surveysDir, tempDir)) {
                FileUtils.copyDirectory(surveysDir, tempDir);
                return true;
            } else {
                Dialogs.alert(context, R.string.warning, R.string.backup_not_enough_space_internal);
                return false;
            }
        }

        private boolean copyBackupFromTempToSurveysDir() throws IOException {
            if (AndroidFiles.enoughSpaceToCopy(tempDir, surveysDir)) {
                if (surveysDir.exists()) {
                    File snapshotSurveysDir = snapshotsManager.newSnapshotDir();
                    FileUtils.moveDirectory(surveysDir, snapshotSurveysDir);
                    AndroidFiles.makeDiscoverable(snapshotSurveysDir, context);
                }
                FileUtils.moveDirectory(tempDir, surveysDir);
                AndroidFiles.makeDiscoverable(surveysDir, context);
                return true;
            } else {
                Dialogs.alert(context, R.string.warning, R.string.backup_not_enough_space_working_directory);
                return false;
            }
        }

        private void showBackupCompleteMessage() {
            String message = context.getString(R.string.backup_complete, AppDirs.root(context));
            Dialogs.info(context, R.string.info, message);
        }

        private void showBackupErrorMessage(Exception e) {
            String message = context.getResources().getString(R.string.backup_failed, e.getMessage());
            Log.e("Backup", message, e);
            Dialogs.alert(context, R.string.warning, message);
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
                                case 2:
                                    backupExecutor.backupIntoDownloads();
                                    break;
                                case 3:
                                    backupExecutor.backupAndShare();
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
