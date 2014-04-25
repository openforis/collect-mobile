package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

/**
 * @author Daniel Wiell
 */
public class TextAttributeComponent extends EditTextAttributeComponent<UiTextAttribute> {
    public TextAttributeComponent(UiTextAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    protected boolean hasChanged(String newValue) {
        return !StringUtils.equals(newValue, attribute.getText());
    }

    protected String attributeValue() {
        return attribute.getText();
    }

    protected void updateAttributeValue(String newValue) {
        attribute.setText(newValue);
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }


}
