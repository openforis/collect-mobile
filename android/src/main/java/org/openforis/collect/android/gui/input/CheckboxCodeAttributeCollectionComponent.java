package org.openforis.collect.android.gui.input;

import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openforis.collect.android.gui.util.Views.px;

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
                focusOnMessageContainerView();
                labelView.setError(message);
            }
        });
    }

    @Override
    protected TextView errorMessageContainerView() {
        return (TextView) ((ViewGroup) toInputView().getParent().getParent()).findViewById(R.id.node_label); // TODO: Ugly!!!
    }

    protected void resetValidationErrors() {

    }

    protected void initOptions() {
        codeByViewId.clear();
        layout.removeAllViews();
        executor.execute(new LoadCodesTask());
    }

    protected void resetSelection() {
        attributesByCode.clear();
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
        if (codeList == null || qualifierInput == null)
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
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(px(context, 1), px(context, 1),
                            px(context, 1), px(context, 15));

                    qualifierInput = createQualifierInput();
                    java.util.List<UiCode> codes = codeList.getCodes();
                    for (int i = 0; i < codes.size(); i++) {
                        final UiCode code = codes.get(i);
                        final boolean qualifiable = codeList.isQualifiable(code);
                        CheckBox cb = new AppCompatCheckBox(context);
                        cb.setId(i + 1);
                        cb.setText(code.toString());
                        cb.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                        cb.setLayoutParams(layoutParams);
                        layout.addView(cb);
                        codeByViewId.put(cb.getId(), code);
                        boolean checked = attributesByCode.keySet().contains(code);
                        if (checked) {
                            cb.setSelected(true);
                            cb.setChecked(true);
                            if (qualifiable)
                                showQualifier();
                        }
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
