package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.widget.EditText;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

/**
 * @author Daniel Wiell
 */
public class BarcodeTextAttributeComponent extends EditTextAttributeComponent<UiTextAttribute> {

    public BarcodeTextAttributeComponent(UiTextAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected String attributeValue() {
        return attribute.getText();
    }

    @Override
    protected void updateAttributeValue(String newValue) {
        attribute.setText(newValue);
    }

    @Override
    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }



}
