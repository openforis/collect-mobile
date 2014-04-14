package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeCollectionComponent extends SavableComponent {
    protected final UiAttributeCollection attributeCollection;

    protected AttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, Context context) {
        super(surveyService, context);
        this.attributeCollection = attributeCollection;
    }

    /**
     * Provide an click listener to get the add button next to the label.
     */
    protected View.OnClickListener onAddAttribute() {
        return null; // No add button by default
    }

    protected abstract void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors);

    protected abstract void resetValidationErrors();

    /**
     * Updates the {@code UiAttributeCollection} {@code UiAttribute} values that are different from the user input.
     *
     * @return the changed attributes.
     */
    protected abstract Set<UiAttribute> updateChangedAttributes();

    public void onAttributeChange(UiAttribute attribute) {
        // Do nothing by default
    }

    public int getViewResource() {
        return onAddAttribute() == null
                ? R.layout.fragment_attribute_detail
                : R.layout.attribute_collection_detail;
    }

    public void setupView(ViewGroup view) {
        super.setupView(view);
        View.OnClickListener addListener = onAddAttribute();
        if (addListener == null)
            return;

        final View addAttributeButton = view.findViewById(R.id.action_add_node);
        if (addAttributeButton == null)
            throw new IllegalStateException(getClass().getSimpleName() +
                    " specifies onAddAttribute, but view doesn't contain button with id action_add_node");
        addAttributeButton.setOnClickListener(addListener);
    }

    protected final void notifyAboutAttributeCollectionChange(Set<UiAttribute> changedAttributes) {
        surveyService.updateAttributes(changedAttributes);
    }

    public final void saveNode() {
        resetValidationErrors();
        notifyAboutAttributeCollectionChange(updateChangedAttributes());
    }

    public final void validateNode() {
        for (UiNode uiNode : attributeCollection.getChildren())
            validateAttribute((UiAttribute) uiNode);
    }

    public final void onValidationError(Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute) {
        for (UiAttribute attribute : validationErrorsByAttribute.keySet())
            if (isInAttributeCollection(attribute)) {
                Set<UiValidationError> validationErrors = validationErrorsByAttribute.get(attribute);
                if (validationErrors != null)
                    setValidationError(attribute, validationErrors);
            }
    }

    private boolean isInAttributeCollection(UiAttribute attribute) {
        return attributeCollection.containsChildWithId(attribute.getId());
    }

    private void validateAttribute(UiAttribute attribute) {
        Set<UiValidationError> validationErrors = attribute.getValidationErrors();
        if (validationErrors != null)
            setValidationError(attribute, validationErrors);
    }

    public String toString() {
        return getClass().getSimpleName() + " for " + attributeCollection;
    }
}
