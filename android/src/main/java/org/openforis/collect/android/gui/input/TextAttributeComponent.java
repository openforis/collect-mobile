package org.openforis.collect.android.gui.input;

import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

/**
 * @author Daniel Wiell
 */
public class TextAttributeComponent extends EditTextAttributeComponent<UiTextAttribute> {
    public TextAttributeComponent(UiTextAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
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
