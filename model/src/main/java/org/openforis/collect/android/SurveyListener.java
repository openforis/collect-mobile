package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface SurveyListener {

    void onNodeSelected(UiNode previous, UiNode selected);

    void onNodeChanging(UiNode node);

    void onNodeChanged(NodeEvent event, UiNode node, Map<UiNode, UiNodeChange> nodeChanges);

    void onRecordEditLockChange(UiRecord record, boolean locked);
}
