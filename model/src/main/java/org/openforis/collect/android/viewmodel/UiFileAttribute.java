package org.openforis.collect.android.viewmodel;

import java.io.File;

/**
 * @author Daniel Wiell
 */
public class UiFileAttribute extends UiAttribute {
    private File file;


    public UiFileAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public synchronized File getFile() {
        return file;
    }

    public synchronized void setFile(File file) {
        this.file = file;
    }

    public boolean isEmpty() {
        return file == null;
    }

    public String toString() {
        return getLabel() + ": " + (file == null ? "Unspecified" : file);
    }
}
