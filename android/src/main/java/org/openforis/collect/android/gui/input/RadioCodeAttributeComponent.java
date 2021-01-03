package org.openforis.collect.android.gui.input;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.components.ExtendedRadioButton;
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
    private EditText qualifierInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Integer selectedViewId;

    RadioCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        qualifierInput = CodeAttributeComponent.createQualifierInput(context, attribute.getQualifier(), new Runnable() {
            public void run() {
                saveNode();
            }
        });
        initOptions();
    }

    protected UiCode selectedCode() {
        return selectedViewId == null ? null : codeByViewId.get(selectedViewId);
    }

    protected View toInputView() {
        return layout;
    }

    public View getDefaultFocusedView() {
        return codeList == null ? null :
                codeList.isQualifiable(selectedCode()) ? qualifierInput : null;
    }

    protected void initOptions() {
        selectedViewId = null;
        codeByViewId.clear();
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

                    for (int i = 0; i < codes.size(); i++) {
                        UiCode code = codes.get(i);
                        boolean selected = isAttributeCode(code);
                        ExtendedRadioButton rb = addRadioButton(layoutParams, i, code, selected);

                        if (selected) {
                            selectedViewId = rb.getId();
                        }
                    }
                }
            });
        }
    }

    private ExtendedRadioButton addRadioButton(RadioGroup.LayoutParams layoutParams, int index, UiCode code, boolean selected) {
        if (! enumerator || selected) { //if it's enumerator, show only selected code
            ExtendedRadioButton rb = new ExtendedRadioButton(context);
            rb.setId(index + 1);
            rb.setLabel(code.toString());
            rb.setDescription(code.getDescription());
            rb.setLayoutParams(layoutParams);
            if (!enumerator) {
                rb.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ExtendedRadioButton view = (ExtendedRadioButton) v;
                        UiCode code = codeByViewId.get(view.getId());
                        boolean wasChecked = isAttributeCode(code);
                        if (selectedViewId != null) {
                            ((ExtendedRadioButton) getContext().findViewById(selectedViewId)).setChecked(false);
                        }
                        boolean checked = !wasChecked;
                        view.setChecked(checked);
                        selectedViewId = checked ? view.getId() : null;

                        if (codeList.isQualifiable(selectedCode()))
                            showQualifier();
                        else
                            hideQualifier();

                        saveNode();
                    }
                });
            }
            rb.setChecked(selected);
            layout.addView(rb);
            codeByViewId.put(rb.getId(), code);
            return rb;
        } else {
            return null;
        }
    }

}
