package org.openforis.collect.android.gui.input;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.openforis.collect.android.gui.util.Views.px;

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
                    layoutParams.setMargins(px(context, 1), px(context, 1),
                            px(context, 1), px(context, 15));

                    Integer selectedViewId = null;
                    for (int i = 0; i < codes.size(); i++) {
                        UiCode code = codes.get(i);
                        boolean selected = isAttributeCode(code);
                        RadioButton rb = addRadioButton(layoutParams, i, code, selected);

                        if (selected) {
                            selectedViewId = rb.getId();
                        }
                    }
                    if (selectedViewId != null) {
                        radioGroup.check(selectedViewId);
                    }
                }
            });
        }
    }

    private RadioButton addRadioButton(RadioGroup.LayoutParams layoutParams, int index, UiCode code, boolean selected) {
        if (! enumerator || selected) { //if it's enumerator, show only selected code
            RadioButton rb = new AppCompatRadioButton(context);
            rb.setId(index + 1);
            rb.setText(code.toString());
            rb.setTextAppearance(context, android.R.style.TextAppearance_Medium);
            rb.setLayoutParams(layoutParams);
            if (!enumerator) {
                rb.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        UiCode code = codeByViewId.get(view.getId());
                        boolean wasSelected = isAttributeCode(code);
                        if (wasSelected) {
                            radioGroup.clearCheck();
                            view.setSelected(true);
                        } else {
                            radioGroup.check(view.getId());
                        }

                        if (codeList.isQualifiable(selectedCode()))
                            showQualifier();
                        else
                            hideQualifier();
                        saveNode();
                    }
                });
            }
            rb.setChecked(selected);
            radioGroup.addView(rb);
            codeByViewId.put(rb.getId(), code);
            return rb;
        } else {
            return null;
        }
    }

}
