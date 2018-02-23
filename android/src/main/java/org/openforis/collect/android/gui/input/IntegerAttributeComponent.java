package org.openforis.collect.android.gui.input;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;

import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * @author Daniel Wiell
 */
public class IntegerAttributeComponent extends NumericAttributeComponent<UiIntegerAttribute, Integer> {
    public IntegerAttributeComponent(UiIntegerAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected Integer attributeNumericValue() {
        return attribute.getValue();
    }

    @Override
    protected String format(Integer value) {
        return value.toString();
    }

    @Override
    protected Integer parse(String value) throws Exception {
        return Integer.parseInt(value);
    }

    @Override
    protected void setValueOnAttribute(Integer value) {
        attribute.setValue(value);
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)}); //10 digits
    }
}
