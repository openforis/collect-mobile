package org.openforis.collect.android.gui.input;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeCollectionComponent extends SavableComponent {
    protected final UiAttributeCollection attributeCollection;
    private View addAttributeButton;

    protected AttributeCollectionComponent(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        super(surveyService, context);
        this.attributeCollection = attributeCollection;
    }

    /**
     * Provide a click listener to get the add button next to the label.
     */
    protected View.OnClickListener onAddAttribute() {
        return null; // No add button by default
    }

    protected abstract void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors);

    /**
     * Updates the {@code UiAttributeCollection} {@code UiAttribute} values that are different from the user input.
     *
     * @return the changed attributes.
     */
    protected abstract Set<UiAttribute> updateChangedAttributes();

    public final void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        if (node instanceof UiAttribute)
            onAttributeChange((UiAttribute) node);
        for (Map.Entry<UiNode, UiNodeChange> changeEntry : nodeChanges.entrySet()) {
            if (isInAttributeCollection(changeEntry.getKey())) {
                Set<UiValidationError> validationErrors = changeEntry.getValue().validationErrors;
                if (validationErrors != null && node instanceof UiAttribute)
                    setValidationError((UiAttribute) node, validationErrors);
            }
        }
    }

    void onAttributeChange(UiAttribute attribute) {
        // Do nothing
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

        addAttributeButton = view.findViewById(R.id.action_add_node);
        if (addAttributeButton == null)
            throw new IllegalStateException(getClass().getSimpleName() +
                    " specifies onAddAttribute, but view doesn't contain button with id action_add_node");
        addAttributeButton.setOnClickListener(addListener);
    }

    protected boolean isRecordEditLocked() {
        UiRecord record = attributeCollection == null ? null : attributeCollection.getUiRecord();
        return record == null ? true: record.isEditLocked();
    }
    @Override
    protected void updateEditableState() {

    }

    protected final void notifyAboutAttributeCollectionChange(Set<UiAttribute> changedAttributes) {
        surveyService.updateAttributes(changedAttributes);
    }

    public void saveNode() {
        resetValidationErrors();
        notifyAboutAttributeCollectionChange(updateChangedAttributes());
    }

    public final void validateNode() {
        for (UiNode uiNode : attributeCollection.getChildren())
            validateAttribute((UiAttribute) uiNode);
    }

    private boolean isInAttributeCollection(UiNode node) {
        return attributeCollection == node || attributeCollection.containsChildWithId(node.getId());
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
