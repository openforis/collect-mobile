package org.openforis.collect.android.gui.input;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.UiAttribute;

/**
 * @author Daniel Wiell
 */
public abstract class EditTextAttributeComponent<T extends UiAttribute> extends AttributeComponent<T> {

    protected EditText editText;

    protected EditTextAttributeComponent(T attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        initializeInputView();
    }

    protected void initializeInputView() {
        initializeEditText();
    }

    protected EditText initializeEditText() {
        editText = createEditText();
        return editText;
    }

    protected abstract String attributeValue();

    protected String editTextToAttributeValue(String text) {
        return text;
    }

    protected abstract void updateAttributeValue(String newValue);

    protected void onEditTextCreated(EditText input) {}

    protected void afterEditTextCreated(EditText input) {}

    protected TextView errorMessageContainerView() {
        return editText;
    }

    protected final boolean updateAttributeIfChanged() {
        if (hasChanged()) {
            updateAttributeValue(extractNewAttributeValue());
            return true;
        } else {
            return false;
        }
    }

    protected EditText getEditText() {
        return editText;
    }

    protected View toInputView() {
        return editText;
    }

    public final View getDefaultFocusedView() {
        return editText;
    }

    protected boolean hasChanged(String newValue) {
        if (newValue == null)
            return !attribute.isEmpty();
        return !StringUtils.equals(attributeValue(), newValue);
    }

    private String extractNewAttributeValue() {
        String newValue = StringUtils.trimToNull(getEditTextString());
        String newAttributeValue = editTextToAttributeValue(newValue);
        return newAttributeValue;
    }

    @Override
    public boolean hasChanged() {
        String editTextValue = StringUtils.trimToNull(getEditTextString());
        String newAttributeValue = extractNewAttributeValue();
        return hasChanged(newAttributeValue) ||
                // formatted value (e.g. number) could be shorter (with less precision) than the text in the input field
                StringUtils.length(editTextValue) > StringUtils.length(newAttributeValue);
    }

    private String getEditTextString() {
        return editText.getText() == null ? null : editText.getText().toString();
    }

    protected String formattedAttributeValue() {
        return attributeValue();
    }

    protected EditText createEditText() {
        final EditText editText = new AppCompatEditText(context);

        onEditTextCreated(editText);

        editText.setSingleLine();

        editText.setText(formattedAttributeValue());

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (AndroidVersion.greaterThan16() && !context.isDestroyed() && !hasFocus) {
                    saveNode();
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    saveNode();
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (hasChanged()) {
                    editText.setError(null);
                    delaySaveNode();
                }
            }
        });
        afterEditTextCreated(editText);
        return editText;
    }
}
