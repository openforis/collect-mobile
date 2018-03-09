package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;

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
        qualifierInput = CodeAttributeComponent.createQualifierInput(context, attribute.getQualifier(), new Runnable() {
            public void run() {
                saveNode();
            }
        });
        radioGroup = new RadioGroup(context);
        layout.addView(radioGroup);
        initOptions();
        if (enumerator) {
            radioGroup.setEnabled(false);
        } else {
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
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(Views.dpsToPixels(context, 1), Views.dpsToPixels(context, 1),
                            Views.dpsToPixels(context, 1), Views.dpsToPixels(context, 15));

                    Integer selectedViewId = null;
                    for (int i = 0; i < codes.size(); i++) {
                        UiCode code = codes.get(i);
                        if (! enumerator || isAttributeCode(code)) { //if it's enumerator, show only selected code
                            RadioButton rb = new AppCompatRadioButton(context);
                            rb.setId(i + 1);
                            rb.setText(code.toString());
                            rb.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                            rb.setLayoutParams(layoutParams);
                            radioGroup.addView(rb);
                            codeByViewId.put(rb.getId(), code);
                            if (isAttributeCode(code)) {
                                selectedViewId = rb.getId();
                                rb.setSelected(true);
                            }
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
