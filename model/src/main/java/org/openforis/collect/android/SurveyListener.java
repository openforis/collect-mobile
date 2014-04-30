package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeChange;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface SurveyListener {
    void onNodeSelected(UiNode previous, UiNode selected);

    void onAttributeChanged(UiAttribute attribute, Map<UiAttribute, UiAttributeChange> attributeChanges);
}
