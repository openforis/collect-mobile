package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public interface SurveyListener {
    void onNodeSelected(UiNode previous, UiNode selected);

    void onAttributeChanged(UiAttribute attribute, Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute);
}
