package org.openforis.collect.android.collectadapter;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.android.util.FileUtils;
import org.openforis.collect.io.SurveyBackupJob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupGenerator {
    private static final Logger LOG = Logger.getLogger(BackupGenerator.class.getName());

    private File sourceDir;
    private String appVersion;
    private File destFile;
    private ZipOutputStream zipOutputStream;

    public BackupGenerator(File sourceDir, String appVersion, File destFile) {
        this.sourceDir = sourceDir;
        this.appVersion = appVersion;
        this.destFile = destFile;
    }

    public void generate() throws IOException {
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(destFile));
            addInfoFile();
            addSourceFiles();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
    }

    private void addSourceFiles() throws IOException {
        List<File> files = FileUtils.listFilesRecursively(sourceDir);
        for (File file: files) {
            String filePath = file.getAbsolutePath();
            writeFile(file, filePath.substring(sourceDir.getAbsolutePath().length() + 1, filePath.length()));
        }
    }

    private void addInfoFile() throws IOException {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(SurveyBackupJob.INFO_FILE_NAME));
            BackupInfo info = new BackupInfo(appVersion);
            info.store(zipOutputStream);
        } finally {
            zipOutputStream.closeEntry();
        }
    }

    private void writeFile(File file, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(entry);
        IOUtils.copy(new FileInputStream(file), zipOutputStream);
        zipOutputStream.closeEntry();
        zipOutputStream.flush();
    }
}
