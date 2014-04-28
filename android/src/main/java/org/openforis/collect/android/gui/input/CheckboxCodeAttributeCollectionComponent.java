package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Wiell
 */
class CheckboxCodeAttributeCollectionComponent extends CodeAttributeCollectionComponent {
    private final SparseArray<UiCode> codeByViewId = new SparseArray<UiCode>();
    private final Map<UiCode, UiCodeAttribute> attributesByCode = new HashMap<UiCode, UiCodeAttribute>();
    private final LinearLayout view;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    CheckboxCodeAttributeCollectionComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, codeListService, surveyService, context);
        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        for (UiNode uiNode : attributeCollection.getChildren()) {
            UiCodeAttribute attribute = (UiCodeAttribute) uiNode;
            attributesByCode.put(attribute.getCode(), attribute);
        }
        initOptions();
    }

    protected Set<UiAttribute> updateChangedAttributes() {
        return new HashSet<UiAttribute>();
    }

    protected View toInputView() {
        return view;
    }

    // TODO: Dry - same as in AttributeComponent
    protected void setValidationError(UiAttribute attribute, final Set<UiValidationError> validationErrors) {
        uiHandler.post(new Runnable() {
            public void run() {
                if (!isSelected())
                    return;
                TextView labelView = errorMessageContainerView();
                if (validationErrors == null || validationErrors.isEmpty()) {
                    labelView.setError(null);
                    return;
                }
                StringBuilder message = new StringBuilder();
                for (UiValidationError validationError : validationErrors)
                    message.append(validationError);
                focus(labelView);
                labelView.setError(message);
            }
        });
    }

    protected TextView errorMessageContainerView() {
        return (TextView) ((ViewGroup) toInputView().getParent().getParent()).findViewById(R.id.node_label); // TODO: Ugly!!!
    }

    protected void resetValidationErrors() {

    }


    private void initOptions() {
        codeByViewId.clear();
        view.removeAllViews();
        executor.execute(new LoadCodesTask());
    }

    private class LoadCodesTask implements Runnable {
        public void run() {
            List<UiCode> codes = codeListService.codeList(attributeCollection);
            addCheckBoxes(codes);
        }

        private void addCheckBoxes(final List<UiCode> codes) {
            uiHandler.post(new Runnable() {
                public void run() {
                    for (final UiCode code : codes) {
                        CheckBox checkBox = new CheckBox(context);
                        checkBox.setText(code.toString());
                        view.addView(checkBox);
                        codeByViewId.put(checkBox.getId(), code);
                        if (attributesByCode.keySet().contains(code)) {
                            checkBox.setSelected(true);
                            checkBox.setChecked(true);
                        }
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked)
                                    attributesByCode.put(code, surveyService.addCodeAttribute(code));
                                else {
                                    surveyService.removeAttribute(attributesByCode.get(code));
                                    attributesByCode.remove(code);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
