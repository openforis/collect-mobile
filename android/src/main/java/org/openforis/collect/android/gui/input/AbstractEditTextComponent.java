package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
abstract class AbstractEditTextComponent<T extends UiAttribute> extends AttributeInputComponent<T> {
    private final EditText editText;

    AbstractEditTextComponent(T attribute, Context context) {
        super(attribute, context);
        this.editText = createEditText();
    }

    public View getView() {
        return editText;
    }

    public void updateAttribute() {
        editText.setError(null);
        String newValue = editText.getText().toString();
        if (StringUtils.isEmpty(newValue)) newValue = null;
        if (hasChanged(attribute(), newValue)) {
            updateAttributeValue(attribute(), newValue);
            notifyAboutAttributeChange();
        }
    }

    public View getDefaultFocusedView() {
        return editText;
    }

    protected abstract String getAttributeValue(T attribute);

    protected abstract void updateAttributeValue(T attribute, String newValue);

    protected abstract boolean hasChanged(T attribute, String newValue);

    protected void onEditTextCreated(EditText input) {

    }

    private EditText createEditText() {
        final EditText editText = new EditText(context());
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    updateAttribute();
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    updateAttribute();
                return false;
            }
        });
        editText.setText(getAttributeValue(attribute()));
        editText.setSingleLine();
        onEditTextCreated(editText);
        return editText;
    }

    public void onSelected() {
        Set<UiValidationError> validationErrors = attribute().getValidationErrors();
        if (validationErrors != null && !validationErrors.isEmpty()) // TODO: Get rid of AttributeValidationError - there can be no other validation errors
            setValidationError(validationErrors);
    }

    public void onValidationError(Set<UiValidationError> validationErrors) {
        super.onValidationError(validationErrors);
        // TODO: This should not be passed when validation is done, but checked every time it's loaded?
        // The validation message must be stored, and shown at other times too.
        setValidationError(validationErrors);
    }

    private void setValidationError(Set<UiValidationError> validationErrors) {
        StringBuilder message = new StringBuilder();
        for (UiValidationError validationError : validationErrors)
            message.append(validationError);
        editText.setError(message);
    }
}
