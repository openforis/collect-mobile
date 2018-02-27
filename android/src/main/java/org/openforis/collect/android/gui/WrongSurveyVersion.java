package org.openforis.collect.android.gui;

import org.openforis.collect.Collect;
import org.openforis.commons.versioning.Version;

import java.io.File;

public class WrongSurveyVersion extends RuntimeException {
    public final String sourceName;
    public final Version version;

    public WrongSurveyVersion(String surveyPath, Version version) {
        super("Survey design exported from Collect Server with newer collect-core version then Collect Mobile. "
                + "Survey exported with " + SurveyImporter.surveyMinorVersion(version)
                + ". Collect Mobile has version " + SurveyImporter.surveyMinorVersion(Collect.VERSION));
        this.sourceName = new File(surveyPath).getName();
        this.version = version;
    }

    public String getSurveyVersion() {
        return SurveyImporter.surveyMinorVersion(version);
    }

    public String getCollectVersion() {
        return SurveyImporter.surveyMinorVersion(Collect.VERSION);
    }
}
