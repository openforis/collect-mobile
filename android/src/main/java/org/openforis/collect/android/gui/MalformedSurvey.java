package org.openforis.collect.android.gui;

import java.io.File;

class MalformedSurvey extends RuntimeException {
    public final String sourceName;

    public MalformedSurvey(String sourcePath, Exception e) {
        super("Failed to import " + sourcePath, e);
        this.sourceName = new File(sourcePath).getName();
    }
}
