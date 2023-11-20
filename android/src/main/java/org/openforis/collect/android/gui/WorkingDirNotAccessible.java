package org.openforis.collect.android.gui;

import java.io.File;

public class WorkingDirNotAccessible extends RuntimeException {
    public WorkingDirNotAccessible(File workingDir) {
        super("workingDir:" + workingDir);
    }
}
