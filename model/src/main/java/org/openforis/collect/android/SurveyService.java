package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public interface SurveyService {
    UiSurvey importSurvey(InputStream inputStream);

    UiSurvey loadSurvey();

    boolean isRecordSelected(int recordId);

    UiRecord selectRecord(int recordId);

    UiRecord addRecord(String rootEntityName);

    UiEntity addEntity();

    UiNode selectNode(int nodeId);

    UiNode lookupNode(int nodeId);

    UiNode selectedNode();

    UiAttribute addAttribute();

    UiCodeAttribute addCodeAttribute(UiCode code, String qualifier);

    void updateAttributes(Set<UiAttribute> attributes);

    void updateAttribute(UiAttribute attribute);

    void removeAttribute(int attributeId);

    void deleteEntities(Collection<Integer> entities);

    void deleteRecords(Collection<Integer> records);

    void exportSurvey() throws IOException;

    void setListener(SurveyListener listener);

    File file(UiFileAttribute attribute);
}
