package org.openforis.collect.android.gui.input;

import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;

import java.text.NumberFormat;

import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * @author Daniel Wiell
 */
public class IntegerAttributeComponent extends NumericAttributeComponent<UiIntegerAttribute, Integer> {

    private static final NumberFormat NUMBER_FORMAT;
    static {
        NUMBER_FORMAT = NumberFormat.getInstance();
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    public IntegerAttributeComponent(UiIntegerAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected Integer attributeNumericValue() {
        return attribute.getValue();
    }

    @Override
    protected String format(Integer value) {
        return NUMBER_FORMAT.format(value);
    }

    @Override
    protected Integer parse(String value) throws Exception {
        return NUMBER_FORMAT.parse(value).intValue();
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
}
