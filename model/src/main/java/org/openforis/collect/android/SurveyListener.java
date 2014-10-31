package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface SurveyListener {

    void onNodeSelected(UiNode previous, UiNode selected);

    void onNodeChanged(NodeEvent event, UiNode node, Map<UiNode, UiNodeChange> nodeChanges);
}
