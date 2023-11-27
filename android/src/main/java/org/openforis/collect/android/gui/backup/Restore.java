package org.openforis.collect.android.gui.backup;

import android.app.Activity;
import android.net.Uri;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.BackupInfo;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.SlowAsyncTask;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.util.Unzipper;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.commons.versioning.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Restore {

    public static void selectFileToRestore(final Activity context) {
        if (Permissions.checkReadExternalStoragePermissionOrRequestIt(context)) {
            ((SurveyNodeActivity) context).setRestoreFileSelectedListener(new RestoreFileSelectedListener() {
                @Override
                public void fileSelected(Uri fileUri) {
                    new RestoreTask(context, fileUri).execute();
                }
            });
            Activities.startFileChooserActivity(context, "Select file to restore", SurveyNodeActivity.RESTORE_FILE_SELECTED_REQUEST_CODE, "*/*");
        }
    }

    private static class RestoreTask extends SlowAsyncTask<Void, Void, Boolean> {

        private Uri fileUri;

        RestoreTask(Activity context, Uri fileUri) {
            super(context, R.string.restore_progress_dialog_title, R.string.please_wait);
            this.fileUri = fileUri;
        }

        @Override
        protected Boolean runTask() throws Exception {
            super.runTask();
            File zipFile = null,
                unzippedDir = null;
            try {
                zipFile = AndroidFiles.copyUriContentToCache(context, fileUri);
                if (!Backup.BACKUP_FILE_EXTENSION.equals(FilenameUtils.getExtension(zipFile.getName()))) {
                    showWarning(R.string.restore_error_invalid_backup_file);
                    return false;
                }
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
                    File infoFile = new File(unzippedDir, SurveyBackupJob.INFO_FILE_NAME);
                    infoFile.delete();
                    FileUtils.moveDirectory(unzippedDir, surveysDir);
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
            File infoPropertiesFile = new File(unzippedDir, SurveyBackupJob.INFO_FILE_NAME);
            if (!infoPropertiesFile.exists()) {
                showWarning(R.string.restore_error_invalid_backup_file);
                return false;
            }
            BackupInfo backupInfo = BackupInfo.parse(new FileInputStream(infoPropertiesFile));
            if (backupInfo.getCollectMobileVersion().compareTo(App.version(context), Version.Significance.MAJOR) < 0) {
                showWarning(R.string.restore_error_backup_file_generated_with_newer_version);
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
    }

    public static interface RestoreFileSelectedListener {
        void fileSelected(Uri fileUri);
    }
}