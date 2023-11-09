package org.openforis.collect.android.gui.input;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.ClearableAutoCompleteTextView;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

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
    private final TextView readonlyTextView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final LinearLayout layout;
    private EditText qualifierInput;
    private TextView qualifierReadonlyText;

    private Map<String, UiCode> uiCodeByValue = new HashMap<String, UiCode>();
    private UiCode selectedCode;

    AutoCompleteCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        autoComplete = new ClearableAutoCompleteTextView(context);
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
                    setSelectedCode(null);
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
        // init readonly textview (visible when attribute is not editable)
        readonlyTextView = new TextView(context);
        readonlyTextView.setTextSize(20);

        qualifierReadonlyText = createQualifierReadonlyText(context, attribute.getQualifier());

        initOptions();
    }

    protected TextView errorMessageContainerView() {
        return autoComplete;
    }

    protected void setSelectedCode(UiCode code) {
        this.selectedCode = code;
        updateEditableState();
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

        readonlyTextView.setText(text);
    }

    @Override
    protected void updateEditableState() {
        boolean editable = !isRecordEditLocked();
        boolean qualifiable = attribute != null  && codeList.isQualifiable(attribute.getCode());
        layout.removeAllViews();
        if (editable) {
            layout.addView(autoComplete);
            if (qualifiable) {
                layout.addView(qualifierInput);
            }
        } else {
            layout.addView(readonlyTextView);
            if (qualifiable) {
                layout.addView(qualifierReadonlyText);
            }
        }
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
