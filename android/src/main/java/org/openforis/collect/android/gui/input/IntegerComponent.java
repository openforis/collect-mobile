package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;

/**
 * @author Daniel Wiell
 */
public class IntegerComponent extends AbstractEditTextComponent<UiIntegerAttribute> {
    IntegerComponent(UiIntegerAttribute attribute, Context context) {
        super(attribute, context);
    }

    protected void updateAttributeValue(UiIntegerAttribute attribute, String newValue) {
        attribute.setValue(newValue == null ? null : Integer.parseInt(newValue));
    }

    protected String getAttributeValue(UiIntegerAttribute attribute) {
        return attribute.getValue() == null ? "" : attribute.getValue().toString();
    }

    protected boolean hasChanged(UiIntegerAttribute attribute, String newValue) {
        return !StringUtils.equals(getAttributeValue(attribute), newValue);
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
    }
}
