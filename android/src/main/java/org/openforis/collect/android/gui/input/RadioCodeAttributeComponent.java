package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Wiell
 */
class RadioCodeAttributeComponent extends CodeAttributeComponent {
    private final SparseArray<UiCode> codeByViewId = new SparseArray<UiCode>();
    private final LinearLayout layout;
    private final RadioGroup radioGroup;
    private EditText qualifierInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    RadioCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        qualifierInput = createQualifierInput();
        radioGroup = new RadioGroup(context);
        layout.addView(radioGroup);
        initOptions();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (codeList.isQualifiable(selectedCode()))
                    showQualifier();
                else
                    hideQualifier();
                saveNode();
            }
        });
    }

    protected UiCode selectedCode() {
        int viewId = radioGroup.getCheckedRadioButtonId();
        return codeByViewId.get(viewId);
    }

    protected View toInputView() {
        return layout;
    }

    public View getDefaultFocusedView() {
        return codeList == null ? null :
                codeList.isQualifiable(selectedCode()) ? qualifierInput : null;
    }

    protected void initOptions() {
        codeByViewId.clear();
        radioGroup.removeAllViews();
        executor.execute(new LoadCodesTask());
    }

    protected String qualifier(UiCode selectedCode) {
        return qualifierInput.getText().toString();
    }


    private EditText createQualifierInput() {
        final EditText editText = new AppCompatEditText(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    saveNode();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    saveNode();
                return false;
            }
        });
        editText.setText(attribute.getQualifier());
        editText.setSingleLine();
        return editText;
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
        public void run() {
            initCodeList();
            addRadioButtons(codeList.getCodes());
            if (codeList.isQualifiable(attribute.getCode()))
                showQualifier();
        }

        private void addRadioButtons(final List<UiCode> codes) {
            uiHandler.post(new Runnable() {
                public void run() {
                    Integer selectedViewId = null;
                    for (int i = 0; i < codes.size(); i++) {
                        UiCode code = codes.get(i);
                        RadioButton radioButton = new AppCompatRadioButton(context);
                        radioButton.setId(i + 1);
                        radioButton.setText(code.toString());
                        radioGroup.addView(radioButton);
                        codeByViewId.put(radioButton.getId(), code);
                        if (isAttributeCode(code)) {
                            selectedViewId = radioButton.getId();
                            radioButton.setSelected(true);
                        }
                    }
                    if (selectedViewId != null) {
                        radioGroup.check(selectedViewId);
                    }
                }
            });
        }
    }

}
