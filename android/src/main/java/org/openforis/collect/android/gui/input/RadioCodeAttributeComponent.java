package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private final RadioGroup radioGroup;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    RadioCodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        radioGroup = new RadioGroup(context);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveNode();
            }
        });
        initOptions();
    }

    protected UiCode selectedCode() {
        int viewId = radioGroup.getCheckedRadioButtonId();
        return codeByViewId.get(viewId);
    }

    protected View toInputView() {
        return radioGroup;
    }

    protected void initOptions() {
        codeByViewId.clear();
        radioGroup.removeAllViews();
        executor.execute(new LoadCodesTask());
    }

    private class LoadCodesTask implements Runnable {

        public void run() {
            List<UiCode> codes = codeListService.codeList(attribute);
            addRadioButtons(codes);
        }

        private void addRadioButtons(final List<UiCode> codes) {
            uiHandler.post(new Runnable() {
                public void run() {
                    Integer selectedViewId = null;
                    for (UiCode code : codes) {
                        RadioButton radioButton = new RadioButton(context);
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
