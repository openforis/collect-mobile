package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;

/**
 * @author Daniel Wiell
 */
public class DoubleComponent extends AbstractEditTextComponent<UiDoubleAttribute> {
    DoubleComponent(UiDoubleAttribute attribute, Context context) {
        super(attribute, context);
    }

    protected void updateAttributeValue(UiDoubleAttribute attribute, String newValue) {
        attribute.setValue(newValue == null ? null : Double.parseDouble(newValue));
    }

    protected String getAttributeValue(UiDoubleAttribute attribute) {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected boolean hasChanged(UiDoubleAttribute attribute, String newValue) {
        return !StringUtils.equals(getAttributeValue(attribute), newValue);
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL);
    }
}
