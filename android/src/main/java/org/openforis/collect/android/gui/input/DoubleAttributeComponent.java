package org.openforis.collect.android.gui.input;

import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * @author Daniel Wiell
 */
public class DoubleAttributeComponent extends NumericAttributeComponent<UiDoubleAttribute, Double> {

    public DoubleAttributeComponent(UiDoubleAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected Double attributeNumericValue() {
        return attribute.getValue();
    }

    @Override
    protected Double parse(String value) throws Exception {
        return numberTextWatcher.getNumberFormat().parse(value).doubleValue();
    }

    @Override
    protected void setValueOnAttribute(Double value) {
        attribute.setValue(value);
    }

    protected void afterEditTextCreated(EditText input) {
        super.afterEditTextCreated(input);
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
        input.setKeyListener(new DecimalSeparatorAwareKeyListener());
    }
}
