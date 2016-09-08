package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Daniel Wiell
 */
class CheckboxCodeAttributeCollectionComponent extends CodeAttributeCollectionComponent {
    private final SparseArray<UiCode> codeByViewId = new SparseArray<UiCode>();
    private final Map<UiCode, UiCodeAttribute> attributesByCode = new HashMap<UiCode, UiCodeAttribute>();
    private final LinearLayout layout;
    private EditText qualifierInput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AtomicBoolean qualified = new AtomicBoolean();

    CheckboxCodeAttributeCollectionComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        for (UiNode uiNode : attributeCollection.getChildren()) {
            UiCodeAttribute attribute = (UiCodeAttribute) uiNode;
            attributesByCode.put(attribute.getCode(), attribute);
        }
        initOptions();
    }

    public void saveNode() {
        super.saveNode();
        saveQualifier();
    }

    protected Set<UiAttribute> updateChangedAttributes() {
        return new HashSet<UiAttribute>();
    }

    protected View toInputView() {
        return layout;
    }

    public View getDefaultFocusedView() {
        return qualified.get() ? qualifierInput : null;
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
        layout.removeAllViews();
        executor.execute(new LoadCodesTask());
    }

    private EditText createQualifierInput() {
        final EditText editText = new AppCompatEditText(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    saveQualifier();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    saveQualifier();
                return false;
            }
        });
        editText.setText(qualifier(codeList.getQualifiableCode()));
        editText.setSingleLine();
        return editText;
    }

    private void saveQualifier() {
        if (codeList == null)
            return;
        UiCode code = codeList.getQualifiableCode();
        if (code == null)
            return;
        UiCodeAttribute attribute = attributesByCode.get(code);
        String newQualifier = qualifierInput.getText().toString().trim();
        if (attribute != null && ObjectUtils.notEqual(newQualifier, attribute.getQualifier())) {
            attribute.setQualifier(newQualifier);
            surveyService.updateAttribute(attribute);
        }
    }

    private void showQualifier() {
        qualified.set(true);
        uiHandler.post(new Runnable() {
            public void run() {
                if (layout.getChildCount() == codeList.getCodes().size()) {
                    layout.addView(qualifierInput);
                    showKeyboard(qualifierInput);
                }
            }
        });
    }


    private String qualifier(UiCode code) {
        UiCodeAttribute attribute = attributesByCode.get(code);
        return attribute == null ? null : attribute.getQualifier();
    }

    private void hideQualifier() {
        qualified.set(false);
        uiHandler.post(new Runnable() {
            public void run() {
                Keyboard.hide(context);
                layout.removeView(qualifierInput);
            }
        });
    }

    private class LoadCodesTask implements Runnable {
        public void run() {
            initCodeList();
            initView(codeList);
        }

        private void initView(final UiCodeList codeList) {
            uiHandler.post(new Runnable() {
                public void run() {
                    qualifierInput = createQualifierInput();
                    java.util.List<UiCode> codes = codeList.getCodes();
                    for (int i = 0; i < codes.size(); i++) {
                        final UiCode code = codes.get(i);
                        final boolean qualifiable = codeList.isQualifiable(code);
                        CheckBox checkBox = new AppCompatCheckBox(context);
                        checkBox.setId(i + 1);
                        checkBox.setText(code.toString());
                        layout.addView(checkBox);
                        codeByViewId.put(checkBox.getId(), code);
                        boolean checked = attributesByCode.keySet().contains(code);
                        if (checked) {
                            checkBox.setSelected(true);
                            checkBox.setChecked(true);
                            if (qualifiable)
                                showQualifier();
                        }
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    attributesByCode.put(code, surveyService.addCodeAttribute(code, qualifier(code)));
                                    if (qualifiable)
                                        showQualifier();
                                } else {
                                    int attributeId = attributesByCode.get(code).getId();
                                    surveyService.deletedAttribute(attributeId);
                                    attributesByCode.remove(code);
                                    if (qualifiable)
                                        hideQualifier();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
