package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.viewmodel.AttributeValidationError;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class TextComponent extends AbstractEditTextComponent<UiTextAttribute> {
    TextComponent(UiTextAttribute attribute, Context context) {
        super(attribute, context);
    }

    protected String getAttributeValue(UiTextAttribute attribute) {
        return attribute.getText();
    }

    protected void updateAttributeValue(UiTextAttribute attribute, String newValue) {
        attribute.setText(newValue);
    }

    protected boolean hasChanged(UiTextAttribute attribute, String newValue) {
        return !StringUtils.equals(newValue, attribute.getText());
    }

    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

}
