package org.openforis.collect.android.gui.input;

import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import java.text.NumberFormat;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * @author Daniel Wiell
 */
public class DoubleAttributeComponent extends NumericAttributeComponent<UiDoubleAttribute, Double> {

    private static final NumberFormat NUMBER_FORMAT;
    static {
        NUMBER_FORMAT = NumberFormat.getInstance();
        NUMBER_FORMAT.setGroupingUsed(true);
        NUMBER_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
        NUMBER_FORMAT.setMaximumIntegerDigits(Integer.MAX_VALUE);
    }

    public DoubleAttributeComponent(UiDoubleAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected Double attributeNumericValue() {
        return attribute.getValue();
    }

    @Override
    protected String format(Double value) {
        return NUMBER_FORMAT.format(value);
    }

    @Override
    protected Double parse(String value) throws Exception {
        return NUMBER_FORMAT.parse(value).doubleValue();
    }

    @Override
    protected void setValueOnAttribute(Double value) {
        attribute.setValue(value);
    }

    protected void onEditTextCreated(EditText input) {
        super.onEditTextCreated(input);
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
        input.setKeyListener(new DecimalSeparatorAwareKeyListener());
    }
}
