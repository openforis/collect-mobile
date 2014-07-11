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

    public void unzip(String fileName) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                if (entryName.equals(fileName)) {
                    extractEntry(zipInputStream, fileName);
                    return;
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            throw new FileNotFoundException("Could not find entry with name " + fileName + " in " + zipFile);
        } finally {
            zipInputStream.close();
        }
    }

    private void extractEntry(ZipInputStream zipInputStream, String entryName) throws IOException {
        File newFile = new File(outputFolder + File.separator + entryName);
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
