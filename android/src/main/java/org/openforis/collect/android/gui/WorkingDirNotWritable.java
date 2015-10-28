package org.openforis.collect.android.gui;

import java.io.File;

public class WorkingDirNotWritable extends RuntimeException {
    public WorkingDirNotWritable(File workingDir) {
        super("workingDir:" + workingDir);
    }
}
