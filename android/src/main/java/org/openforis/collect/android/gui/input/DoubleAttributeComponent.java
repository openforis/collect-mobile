package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
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

    protected boolean hasChanged(String newValue) {
        return !StringUtils.equals(attributeValue(), newValue);
    }

    protected String attributeValue() {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected void updateAttributeValue(String newValue) {
        attribute.setValue(newValue == null ? null : Double.parseDouble(newValue));
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }
}
