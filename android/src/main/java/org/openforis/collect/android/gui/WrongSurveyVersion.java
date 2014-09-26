package org.openforis.collect.android.gui;

import org.openforis.collect.Collect;
import org.openforis.commons.versioning.Version;

import java.io.File;

public class WrongSurveyVersion extends RuntimeException {
    public final String sourceName;
    public final Version version;

    public WrongSurveyVersion(String surveyPath, Version version) {
        super("Survey design exported from unsupported Collect Server version. Was "
                + SurveyImporter.surveyMinorVersion(version) + ", requires " + SurveyImporter.surveyMinorVersion(Collect.getVersion()));
        this.sourceName = new File(surveyPath).getName();
        this.version = version;
    }

}
