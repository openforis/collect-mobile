package org.openforis.collect.android.gui.input;

import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;

import org.jooq.tools.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.DecimalNumberTextWatcher;
import org.openforis.collect.android.viewmodel.UiAttribute;

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

    @Override
    protected String editTextToAttributeValue(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            T val = parse(text);
            if (val == null) {
                return null;
            } else {
                return format(val);
            }
        } catch (Exception e) {
            return null;
        }
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

    protected void onEditTextCreated(EditText input) {
        input.addTextChangedListener(new DecimalNumberTextWatcher(input));
    }

    private void setNotANumberError() {
        getEditText().setError(context.getResources().getString(R.string.message_not_a_number));
    }


}
