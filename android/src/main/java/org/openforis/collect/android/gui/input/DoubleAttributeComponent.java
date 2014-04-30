package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;

/**
 * @author Daniel Wiell
 */
public class DoubleAttributeComponent extends EditTextAttributeComponent<UiDoubleAttribute> {
    public DoubleAttributeComponent(UiDoubleAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    protected String attributeValue() {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected boolean hasChanged(String newValue) {
        boolean changed = super.hasChanged(newValue);
        try {
            if (changed && attribute.getValue() != null && newValue != null)
                changed = !attribute.getValue().equals(Double.parseDouble(newValue));
        } catch (NumberFormatException ignore) {
        }
        return changed;
    }

    protected void updateAttributeValue(String newValue) {
        attribute.setValue(newValue == null ? null : Double.parseDouble(newValue));
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }
}
