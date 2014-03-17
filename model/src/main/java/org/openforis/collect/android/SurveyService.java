package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;

import java.io.InputStream;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface SurveyService {
    UiSurvey importSurvey(InputStream inputStream);

    UiSurvey loadSurvey(String name);

    boolean isRecordSelected(int recordId);

    UiRecord selectRecord(int recordId);

    UiRecord addRecord(String rootEntityName);

    UiEntity addEntity();

    UiNode selectNode(int nodeId);

    UiNode lookupNode(int nodeId);

    UiNode selectedNode();

    void updateAttribute(UiAttribute attribute);

    void setListener(SurveyListener listener);
}
