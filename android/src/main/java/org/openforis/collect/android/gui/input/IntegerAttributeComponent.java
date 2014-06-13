package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;

import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * @author Daniel Wiell
 */
public class IntegerAttributeComponent extends EditTextAttributeComponent<UiIntegerAttribute> {
    public IntegerAttributeComponent(UiIntegerAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    protected String attributeValue() {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected void updateAttributeValue(String newValue) {
        attribute.setValue(newValue == null ? null : Integer.parseInt(newValue));
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
    }
}
