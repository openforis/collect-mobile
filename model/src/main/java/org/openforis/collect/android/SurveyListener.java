package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface SurveyListener {
    void onNodeSelected(UiNode previous, UiNode selected);

    void onNodeChanged(UiNode node, Map<UiNode, UiNodeChange> nodeChanges);
}
