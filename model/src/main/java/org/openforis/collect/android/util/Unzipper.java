package org.openforis.collect.android.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

    public void unzip(String... fileNames) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        Set<String> namesToUnzip = new HashSet<String>(Arrays.asList(fileNames));
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null && !namesToUnzip.isEmpty()) {
                String entryName = entryName(zipEntry);
                if (namesToUnzip.contains(entryName)) {
                    extractEntry(zipInputStream, entryName);
                    namesToUnzip.remove(entryName);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            if (!namesToUnzip.isEmpty())
                throw new FileNotFoundException("Could not find entries  " + namesToUnzip + " in " + zipFile);
        } finally {
            zipInputStream.close();
        }
    }

    public void unzipAll() throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        try {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                // keep folders hierarchy
                String entryOutputFileName = zipEntry.getName().replace("/", File.separator);
                extractEntry(zipInputStream, entryOutputFileName);
                zipEntry = zipInputStream.getNextEntry();
            }
        } finally {
            zipInputStream.close();
        }
    }

    private String entryName(ZipEntry zipEntry) {
        String name = zipEntry.getName();
        int i = name.lastIndexOf('/'); // Ignore directories
        return i == -1 ? name : name.substring(i + 1);
    }

    private void extractEntry(ZipInputStream zipInputStream, String outputFileName) throws IOException {
        File newFile = new File(outputFolder + File.separator + outputFileName);
        FileUtils.forceMkdirParent(newFile);
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
