package org.openforis.collect.android.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Daniel Wiell
 */
public class Unzipper {
    private final byte[] buffer = new byte[1024];
    private final File zipFile;
    private final File outputFolder;

    public Unzipper(File zipFile, File outputFolder) {
        this.zipFile = zipFile;
        this.outputFolder = outputFolder;
    }

    public void unzip() throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                extractEntry(zipInputStream, zipEntry);
                zipEntry = zipInputStream.getNextEntry();
            }
        } finally {
            zipInputStream.close();
        }
    }

    private void extractEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        String fileName = zipEntry.getName();
        File newFile = new File(outputFolder + File.separator + fileName);
        OutputStream fos = new FileOutputStream(newFile);
        write(zipInputStream, fos);
    }

    private void write(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            int len;
            while ((len = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, len);
        } finally {
            outputStream.close();
        }
    }
}
