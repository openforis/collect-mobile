package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;

/**
 * @author Daniel Wiell
 */
public class IntegerAttributeComponent extends EditTextAttributeComponent<UiIntegerAttribute> {
    public IntegerAttributeComponent(UiIntegerAttribute attribute, SurveyService surveyService, Context context) {
        super(attribute, surveyService, context);
    }

    protected boolean hasChanged(String newValue) {
        return !StringUtils.equals(attributeValue(), newValue);
    }

    protected String attributeValue() {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected void updateAttributeValue(String newValue) {
        attribute.setValue(newValue == null ? null : Integer.parseInt(newValue));
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
    }
}
