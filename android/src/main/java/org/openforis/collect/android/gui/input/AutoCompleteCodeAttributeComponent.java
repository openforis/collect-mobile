package org.openforis.collect.android.gui.input;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
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
    private final AutoCompleteTextView autoComplete;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, UiCode> uiCodeByValue = new HashMap<String, UiCode>();
    private UiCode selectedCode;

    AutoCompleteCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        autoComplete = new ClearableAutoCompleteTextView(context);
        autoComplete.setThreshold(1);
        autoComplete.setSingleLine();
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCode = (UiCode) autoComplete.getAdapter().getItem(position);
                saveNode();
            }
        });
        autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCode = (UiCode) autoComplete.getAdapter().getItem(position);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                selectedCode = null;
            }
        });
        autoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    saveNode();
                return false;
            }
        });
        initOptions();
    }

    protected TextView errorMessageContainerView() {
        return autoComplete;
    }

    protected UiCode selectedCode() {
        String text = autoComplete.getText().toString();
        if (selectedCode == null || !selectedCode.toString().equals(text)) {
            if (StringUtils.isEmpty(text))
                selectedCode = null;
            else
                selectedCode = uiCodeByValue.get(text.trim());
        }
        if (selectedCode == null) {
            setText("");
            return null;
        }
        setText(selectedCode.toString());
        return selectedCode;
    }

    protected View toInputView() {
        return autoComplete;
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

    private class LoadCodesTask implements Runnable {
        /**
         * Handle bound to main thread, to post updates to AutoCompleteTextView on the main thread.
         * The AutoCompleteTextView itself might not yet be bound to the window.
         */
        Handler uiHandler = new Handler();

        public void run() {
            List<UiCode> codes = codeListService.codeList(attribute);
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
