package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;

import java.text.ParseException;

/**
 * @author Stefano Ricci
 */

public abstract class NumericAttributeComponent<A extends UiAttribute, T extends Number> extends EditTextAttributeComponent<A> {

    protected NumericAttributeComponent(A attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    protected abstract T attributeNumericValue();

    @Override
    protected String attributeValue() {
        T val = attributeNumericValue();
        return  val == null ? "" : format(val);
    }

    protected boolean hasChanged(String newValue) {
        boolean changed = super.hasChanged(newValue);
        try {
            if (changed && newValue != null) {
                //try to parse the new value anyway
                T newVal = parse(newValue);
                T oldValue = attributeNumericValue();
                changed = oldValue == null || !oldValue.equals(newVal);
            }
        } catch (Exception ignore) {
            setNotANumberError();
            //not a number value will be considered as a null value
            changed = !attribute.isEmpty();
        }
        return changed;
    }

    protected void updateAttributeValue(String newValue) {
        T newNumericVal = null;
        try {
            newNumericVal = newValue == null ? null : parse(newValue);
        } catch (Exception e) {
            setNotANumberError();
        }
        setValueOnAttribute(newNumericVal);
    }

    protected abstract void setValueOnAttribute(T value);

    protected abstract String format(T value);

    protected abstract T parse(String value) throws Exception;

    private void setNotANumberError() {
        getEditText().setError(context.getResources().getString(R.string.message_not_a_number));
    }
}
