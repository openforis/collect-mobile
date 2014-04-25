package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Map;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeComponent<T extends UiAttribute> extends SavableComponent {
    protected final T attribute;

    protected AttributeComponent(T attribute,
                                 SurveyService surveyService,
                                 FragmentActivity context) {
        super(surveyService, context);
        this.attribute = attribute;
    }

    protected TextView errorMessageContainerView() {
        return (TextView) ((ViewGroup) toInputView().getParent().getParent()).findViewById(R.id.node_label); // TODO: Ugly!!!
    }

    protected final void setValidationError(final Set<UiValidationError> validationErrors) {
        uiHandler.post(new Runnable() {
            public void run() {
                if (!isSelected())
                    return;
                TextView labelView = errorMessageContainerView();
                if (validationErrors == null || validationErrors.isEmpty()) {
                    labelView.setError(null);
                    return;
                }
                StringBuilder message = new StringBuilder();
                for (UiValidationError validationError : validationErrors)
                    message.append(validationError);
                focus(labelView);
                labelView.setError(message);
            }
        });
    }

    protected final void resetValidationErrors() {
        errorMessageContainerView().setError(null);
    }

    /**
     * Updates the {@code UiAttribute} value if current attribute value is different from the input component value.
     *
     * @return true if attribute changed
     */
    protected abstract boolean updateAttributeIfChanged();

    public void onAttributeChange(UiAttribute attribute) {
        // Do nothing by default
    }

    protected final void notifyAboutAttributeChange() {
        surveyService.updateAttribute(attribute);
    }

    public final int getViewResource() {
        return R.layout.fragment_attribute_detail;
    }

    public final void setupView(ViewGroup view) {
        ViewGroup attributeInputContainer = (ViewGroup) view.findViewById(R.id.input_container);
        view.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        attributeInputContainer.addView(toInputView());
    }

    public final void saveNode() {
        resetValidationErrors(); // TODO: Will reset even if attribute hasn't changed
        if (updateAttributeIfChanged())
            notifyAboutAttributeChange();
    }

    public final void validateNode() {
        Set<UiValidationError> validationErrors = attribute.getValidationErrors();
        if (validationErrors != null)
            setValidationError(validationErrors);
    }

    public final void onValidationError(Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute) {
        Set<UiValidationError> validationErrors = validationErrorsByAttribute.get(attribute);
        if (validationErrors != null)
            setValidationError(validationErrors);
    }

    public String toString() {
        return getClass().getSimpleName() + " for " + attribute;
    }
}
