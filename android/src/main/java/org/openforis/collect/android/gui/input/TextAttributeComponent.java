package org.openforis.collect.android.gui.input;

import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiTextAttribute;
import org.openforis.collect.android.viewmodel.UiTextAttributeDefinition;

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

    @Override
    protected void onEditTextCreated(EditText input) {
        super.onEditTextCreated(input);
        UiTextAttributeDefinition def = (UiTextAttributeDefinition) attribute.getDefinition();
        int inputType = def.isAutoUppercase() ? InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS : InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        input.setInputType(inputType);
        if (def.isAutoUppercase()) {
            input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        }
    }
}
