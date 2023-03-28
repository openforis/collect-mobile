package org.openforis.collect.android.gui.input;

import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.NumberTextWatcher;
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
    protected Integer parse(String value) throws Exception {
        return numberTextWatcher.getNumberFormat().parse(value).intValue();
    }

    @Override
    protected void setValueOnAttribute(Integer value) {
        attribute.setValue(value);
    }

    protected void onEditTextCreated(EditText input) {
        super.onEditTextCreated(input);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)}); //10 digits + 3 grouping characters
    }

    @Override
    protected void initializeNumberTextWatcher(EditText input) {
        numberTextWatcher = new NumberTextWatcher(input, false);
    }
}
