package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import java.text.NumberFormat;

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
        //input.setKeyListener(new DecimalSeparatorAwareKeyListener());
    }
}
