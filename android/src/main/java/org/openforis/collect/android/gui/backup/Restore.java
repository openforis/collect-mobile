package org.openforis.collect.android.gui.backup;

import android.app.Activity;
import android.net.Uri;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.BackupGenerator;
import org.openforis.collect.android.collectadapter.BackupInfo;
import org.openforis.collect.android.gui.BaseActivity;
import org.openforis.collect.android.gui.settings.SettingsActivity;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.SlowJob;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.util.Unzipper;
import org.openforis.commons.versioning.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Restore {

    public static void confirmRestore(final Activity context) {
        Dialogs.confirm(context, R.string.restore_confirm_title, R.string.restore_confirm_message, new Runnable() {
            public void run() {
                selectFileToRestore(context);
            }
        });

    }

    private static void selectFileToRestore(final Activity context) {
        if (Permissions.checkReadExternalStoragePermissionOrRequestIt(context)) {
            ((SettingsActivity) context).setRestoreFileSelectedListener(new RestoreFileSelectedListener() {
                @Override
                public void fileSelected(Uri fileUri) {
                    onFileSelected(fileUri, context);
                }
            });
            Activities.startFileChooserActivity(context, "Select file to restore", SettingsActivity.RESTORE_FILE_SELECTED_REQUEST_CODE, "*/*");
        }
    }

    private static void onFileSelected(final Uri fileUri, final Activity context) {
        String fileName = AndroidFiles.getUriContentFileName(context, fileUri);
        if (!Backup.BACKUP_FILE_EXTENSION.equals(FilenameUtils.getExtension(fileName))) {
            Dialogs.alert(context, R.string.warning, R.string.restore_error_invalid_backup_file);
        } else {
            Dialogs.confirm(context, R.string.restore_confirm_title, R.string.restore_confirm_message_2, new Runnable() {
                public void run() {
                    new RestoreJob(context, fileUri).execute();
                }
            });
        }
    }

    private static class RestoreJob extends SlowJob<Void, Void, Boolean> {

        private Uri fileUri;

        RestoreJob(Activity context, Uri fileUri) {
            super(context, null, R.string.restore_progress_dialog_title, R.string.please_wait);
            this.fileUri = fileUri;
        }

        @Override
        protected Boolean runTask() throws Exception {
            super.runTask();
            File zipFile = null,
                unzippedDir = null;
            try {
                zipFile = AndroidFiles.copyUriContentToCache(context, fileUri);
                unzippedDir = org.openforis.collect.android.util.FileUtils.createTempDir();
                if (AndroidFiles.enoughSpaceToCopy(zipFile, unzippedDir)) {
                    new Unzipper(zipFile, unzippedDir).unzipAll();
                    if (!checkBackupFile(unzippedDir)) {
                        return false;
                    }
                    File surveysDir = AppDirs.surveysDir(context);
                    if (surveysDir.exists() && !backupSurveysDir()) {
                        return false;
                    }
                    File infoFile = new File(unzippedDir, BackupGenerator.INFO_FILE_NAME);
                    infoFile.delete();
                    File unzippedSurveysDir = new File(unzippedDir, BackupGenerator.SURVEYS_DIR);
                    FileUtils.moveDirectory(unzippedSurveysDir, surveysDir);
                    AndroidFiles.makeDiscoverable(surveysDir, context);
                    return true;
                } else {
                    showWarning(R.string.backup_not_enough_space_working_directory);
                    return false;
                }
            } finally {
                if (zipFile != null) zipFile.delete();
                if (unzippedDir != null) FileUtils.deleteDirectory(unzippedDir);
            }
        }

        private boolean checkBackupFile(File unzippedDir) throws IOException {
            File infoPropertiesFile = new File(unzippedDir, BackupGenerator.INFO_FILE_NAME);
            if (!infoPropertiesFile.exists()) {
                showWarning(R.string.restore_error_invalid_backup_file);
                return false;
            }
            BackupInfo backupInfo = BackupInfo.parse(new FileInputStream(infoPropertiesFile));
            if (backupInfo.getCollectMobileVersion().compareTo(App.version(context), Version.Significance.MAJOR) < 0) {
                showWarning(R.string.restore_error_backup_file_generated_with_newer_version);
                return false;
            }
            File surveysDir = new File(unzippedDir, BackupGenerator.SURVEYS_DIR);
            if (!surveysDir.exists() || surveysDir.list().length == 0) {
                showWarning(R.string.restore_error_empty_surveys_folder);
                return false;
            }
            return true;
        }

        private boolean backupSurveysDir() throws IOException {
            File surveysDir = AppDirs.surveysDir(context);
            SnapshotsManager snapshotsManager = new SnapshotsManager(surveysDir);
            File snapshotSurveysDir = snapshotsManager.newSnapshotDir();
            if (AndroidFiles.enoughSpaceToCopy(surveysDir, snapshotSurveysDir)) {
                FileUtils.moveDirectory(surveysDir, snapshotSurveysDir);
                AndroidFiles.makeDiscoverable(snapshotSurveysDir, context);
                return true;
            } else {
                showWarning(R.string.backup_not_enough_space_working_directory);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result != null && result.booleanValue()) {
                BaseActivity.restartMainActivity(context);
            } else {
                String errorDetails = lastException == null ? null : lastException.getMessage();
                showError(R.string.restore_error, errorDetails);
            }
        }
    }

    public static interface RestoreFileSelectedListener {
        void fileSelected(Uri fileUri);
    }
}