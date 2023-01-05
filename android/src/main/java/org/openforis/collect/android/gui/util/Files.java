package org.openforis.collect.android.gui.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public abstract class Files {

    public static File changeExtension(File file, String extension) throws IOException {
        String oldExtension = FilenameUtils.getExtension(file.getName());
        if (StringUtils.equals(extension, oldExtension)) {
            return file;
        }
        String newFileName = FilenameUtils.removeExtension(file.getName()) + "." + extension;
        File newFile = new File(file.getParent(), newFileName);
        if (file.exists()) {
            if (!file.renameTo(newFile)) {
                throw new IOException("Cannot change extension of file: " + file.getAbsolutePath());
            }
            return file;
        } else {
            return newFile;
        }
    }
}
