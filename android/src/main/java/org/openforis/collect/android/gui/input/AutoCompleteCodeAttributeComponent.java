package org.openforis.collect.android.gui.input;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.ClearableAutoCompleteTextView;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.metamodel.ui.UICodeField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Wiell
 */
class AutoCompleteCodeAttributeComponent extends CodeAttributeComponent {
    private final ClearableAutoCompleteTextView autoComplete;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LinearLayout layout;
    private EditText qualifierInput;

    private Map<String, UiCode> uiCodeByValue = new HashMap<String, UiCode>();
    private UiCode selectedCode;

    AutoCompleteCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        autoComplete = new ClearableAutoCompleteTextView(context);
        layout.addView(autoComplete);
        autoComplete.setThreshold(1);
        autoComplete.setSingleLine();
        autoComplete.setHint(R.string.hint_code_autocomplete);
        if (enumerator) {
            autoComplete.setEnabled(false);
        } else {
            autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setSelectedCode((UiCode) autoComplete.getAdapter().getItem(position));
                    saveNode();
                }
            });
            autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setSelectedCode((UiCode) autoComplete.getAdapter().getItem(position));
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    selectedCode = null;
                }
            });
            autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                        saveNode();
                    return false;
                }
            });
            autoComplete.setOnClearListener(new ClearableAutoCompleteTextView.OnClearListener() {
                public void onClear() {
                    autoComplete.setText("");
                    saveNode();
                }
            });
        }
        qualifierInput = createQualifierInput(context, attribute.getQualifier(), new Runnable() {
            public void run() {
                saveNode();
            }
        });
        initOptions();
    }

    protected TextView errorMessageContainerView() {
        return autoComplete;
    }

    protected void setSelectedCode(UiCode code) {
        this.selectedCode = code;
        if (codeList.isQualifiable(selectedCode))
            showQualifier();
        else
            hideQualifier();
    }

    protected UiCode selectedCode() {
        String text = autoComplete.getText().toString();
        if (selectedCode == null || !selectedCode.toString().equals(text)) {
            if (StringUtils.isEmpty(text))
                setSelectedCode(null);
            else
                setSelectedCode(uiCodeByValue.get(text.trim()));
        }
        if (selectedCode == null) {
            setText("");
            return null;
        } else {
            setText(selectedCode.toString());
            return selectedCode;
        }
    }

    protected String qualifier(UiCode selectedCode) {
        return codeList.isQualifiable(selectedCode) ? qualifierInput.getText().toString(): null;
    }

    protected View toInputView() {
        return layout;
    }

    protected void initOptions() {
        if (attribute.getCode() != null) {
            setText(attribute.getCode().toString());
            selectedCode = attribute.getCode();
        }
        executor.execute(new LoadCodesTask());
    }

    public View getDefaultFocusedView() {
        return autoComplete;
    }

    private void setText(String text) {
        if (text.equals(autoComplete.getText().toString()))
            return;
        // Hack to prevent pop-up from opening when setting text
        // http://www.grokkingandroid.com/how-androids-autocompletetextview-nearly-drove-me-nuts/
        UiCodeAdapter adapter = (UiCodeAdapter) autoComplete.getAdapter();
        autoComplete.setAdapter(null);
        autoComplete.setText(text);
        autoComplete.setAdapter(adapter);
    }

    private void showQualifier() {
        uiHandler.post(new Runnable() {
            public void run() {
                if (layout.getChildCount() == 1) {
                    layout.addView(qualifierInput);
                    showKeyboard(qualifierInput);
                }
            }
        });
    }

    private void hideQualifier() {
        uiHandler.post(new Runnable() {
            public void run() {
                hideKeyboard();
                layout.removeView(qualifierInput);
            }
        });
    }

    private class LoadCodesTask implements Runnable {
        /**
         * Handle bound to main thread, to post updates to AutoCompleteTextView on the main thread.
         * The AutoCompleteTextView itself might not yet be bound to the window.
         */
        Handler uiHandler = new Handler();

        public void run() {
            initCodeList();
            List<UiCode> codes = codeList.getCodes();
            for (UiCode code : codes)
                uiCodeByValue.put(code.getValue(), code);
            setAdapter(codes, uiHandler);
            if (codeList.isQualifiable(attribute.getCode()))
                showQualifier();
        }

        private void setAdapter(final List<UiCode> codes, Handler uiHandler) {
            uiHandler.post(new Runnable() {
                public void run() {
                    autoComplete.setAdapter(new UiCodeAdapter(context, codes));
                }
            });
        }
    }
}
