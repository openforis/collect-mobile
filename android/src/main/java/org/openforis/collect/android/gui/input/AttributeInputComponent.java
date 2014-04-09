package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class AttributeInputComponent<T extends UiAttribute> {
    private final T attribute;
    private final Context context;
    protected final SurveyService surveyService;

    protected AttributeInputComponent(T attribute, Context context) {
        this.attribute = attribute;
        this.context = context;
        this.surveyService = ServiceLocator.surveyService();
    }

    public abstract View getView();

    public abstract void updateAttribute();

    public View getDefaultFocusedView() {
        return null;
    }

    /**
     * Invoked when an attribute has changed.
     */
    public void onAttributeChange(UiAttribute attribute) {
        // Empty default implementation
    }

    protected final void notifyAboutAttributeChange() {
        surveyService.updateAttribute(attribute);
    }

    protected final T attribute() {
        return attribute;
    }

    protected final Context context() {
        return context;
    }

    public static AttributeInputComponent create(UiAttribute attribute, Context context) {
        if (attribute instanceof UiTextAttribute)
            return new TextComponent((UiTextAttribute) attribute, context);
        if (attribute instanceof UiIntegerAttribute)
            return new IntegerComponent((UiIntegerAttribute) attribute, context);
        if (attribute instanceof UiDoubleAttribute)
            return new DoubleComponent((UiDoubleAttribute) attribute, context);
        if (attribute instanceof UiTimeAttribute)
            return new TimePickerComponent((UiTimeAttribute) attribute, context);
        if (attribute instanceof UiDateAttribute)
            return new DatePickerComponent((UiDateAttribute) attribute, context);
        if (attribute instanceof UiCodeAttribute)
            return new CodeComponent((UiCodeAttribute) attribute, context);
        if (attribute instanceof UiTaxonAttribute)
            return new TaxonComponent((UiTaxonAttribute) attribute, context);
        else
            return new DummyInputComponent(attribute, context); // TODO: Remove once all are implemented
//            throw new IllegalStateException("Unexpected attribute type: " + attribute.getClass());
    }

    public void onValidationError(Map<UiAttribute, Set<UiValidationError>> validationErrorsByAttribute) {
        Set<UiValidationError> validationErrors = validationErrorsByAttribute.get(attribute);
        if (validationErrors != null && !validationErrors.isEmpty())
            onValidationError(validationErrors);
    }

    public void onValidationError(Set<UiValidationError> validationErrors) {
        // Empty by default
    }

    public void onSelected() {
        // Empty by default
    }

    private static class DummyInputComponent extends AttributeInputComponent {
        @SuppressWarnings("unchecked")
        public DummyInputComponent(UiAttribute attribute, Context context) {
            super(attribute, context);
        }

        public View getView() {
            TextView textView = new TextView(context());
            textView.setText("Attribute type not implemented: " + attribute().getClass().getSimpleName());
            return textView;
        }

        public void updateAttribute() {

        }
    }
}
