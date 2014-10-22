package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import java.text.NumberFormat;
import java.text.ParseException;

import static android.text.InputType.*;

/**
 * @author Daniel Wiell
 */
public class DoubleAttributeComponent extends EditTextAttributeComponent<UiDoubleAttribute> {
    public DoubleAttributeComponent(UiDoubleAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    protected String attributeValue() {
        return attribute.getValue() == null ? "" : format(attribute.getValue());
    }

    protected boolean hasChanged(String newValue) {
        boolean changed = super.hasChanged(newValue);
        try {
            if (changed && attribute.getValue() != null && newValue != null)
                changed = !attribute.getValue().equals(parse(newValue));
        } catch (ParseException ignore) {
            setNotANumberError();
        }
        return changed;
    }

    protected void updateAttributeValue(String newValue) {
        try {
            attribute.setValue(newValue == null ? null : parse(newValue));
        } catch (ParseException e) {
            setNotANumberError();
        }
    }

    private String format(double value) {
        return numberFormat().format(value);
    }

    private double parse(String value) throws ParseException {
        return numberFormat().parse(value).doubleValue();
    }

    private NumberFormat numberFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return numberFormat;
    }

    private void setNotANumberError() {
        getEditText().setError(context.getResources().getString(R.string.message_not_a_number));
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
    }
}
