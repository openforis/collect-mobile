package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.detail.CodeListDescriptionDialogFragment;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCodeList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeComponent extends AttributeComponent<UiCodeAttribute> {

    protected static final int RADIO_GROUP_MAX_SIZE = 100;

    static final String DESCRIPTION_BUTTON_TAG = "descriptionButton";

    private UiCode parentCode;
    final CodeListService codeListService;
    final boolean enumerator;
    protected UiCodeList codeList;
    private boolean codeListRefreshForced;

    CodeAttributeComponent(UiCodeAttribute codeAttribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(codeAttribute, surveyService, context);
        this.codeListService = codeListService;
        this.enumerator = attribute.getDefinition().isEnumerator();
    }

    public static CodeAttributeComponent create(UiCodeAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attribute);
        boolean enumerator = attribute.getDefinition().isEnumerator();
        if (maxCodeListSize <= RADIO_GROUP_MAX_SIZE && !enumerator)
            return new CodeAttributeRadioComponent(attribute, codeListService, surveyService, context);
        else
            return new CodeAttributeAutoCompleteComponent(attribute, codeListService, surveyService, context);
    }

    public final void onAttributeChange(UiAttribute changedAttribute) {
        if (changedAttribute != attribute && codeListService.isParentCodeAttribute(changedAttribute, attribute)) {
            UiCode newParentCode = ((UiCodeAttribute) changedAttribute).getCode();
            if (newParentCode == parentCode) return;
            if (newParentCode == null || !newParentCode.equals(parentCode)) {
                parentCode = newParentCode;
                setCodeListRefreshForced(true);
                initOptions();
            }
        }
    }

    protected abstract CodeValue getSelectedCodeValue();

    protected abstract Set<CodeValue> getAttributeCodeValues();

    protected abstract UiCodeAttributeDefinition getCodeAttributeDefinition();

    protected boolean isValueShown() {
        return getCodeAttributeDefinition().isValueShown();
    }

    private synchronized boolean isCodeListRefreshForced() {
        return codeListRefreshForced;
    }

    private synchronized void setCodeListRefreshForced(boolean codeListRefreshForced) {
        this.codeListRefreshForced = codeListRefreshForced;
    }

    protected final boolean updateAttributeIfChanged() {
        if (codeList == null)
            return false;
        CodeValue newValue = getSelectedCodeValue();
        String newCode = newValue == null ? null : newValue.code;
        String newQualifier = newValue == null ? null : newValue.qualifier;

        if (hasChanged(newCode, newQualifier)) {
            attribute.setCode(codeList.getCode(newCode));
            attribute.setQualifier(newQualifier);
            return true;
        }
        return false;
    }

    protected void initCodeList() {
        if (codeList == null || isCodeListRefreshForced()) {
            setCodeListRefreshForced(false);
            codeList = codeListService.codeList(attribute);
            uiHandler.post(new Runnable() {
                public void run() {
                    if (codeList.containsDescription())
                        includeDescriptionsButtonToParent(context, toInputView());
                }
            });
        }
    }

    static void includeDescriptionsButtonToParent(final FragmentActivity context, View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null)
            return;
        if (parent.findViewWithTag(DESCRIPTION_BUTTON_TAG) == null) {
            Button button = new AppCompatButton(context);
            button.setTextAppearance(context, android.R.style.TextAppearance_Small);
            button.setTag(DESCRIPTION_BUTTON_TAG);
            button.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setText(context.getResources().getString(R.string.label_show_code_descriptions));
            button.setBackgroundDrawable(null);
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CodeListDescriptionDialogFragment.show(context.getSupportFragmentManager());
                }
            });
            int linkColor = new TextView(context).getLinkTextColors().getDefaultColor();
            button.setTextColor(linkColor);
            parent.addView(button);
        }
    }

    private boolean hasChanged(String newCode, String newQualifier) {
        return notEqual(attribute.getCode(), newCode)
                || notEqual(
                trimToEmpty(attribute.getQualifier()),
                trimToEmpty(newQualifier)
        );
    }

    protected abstract void initOptions();

    final boolean isAttributeCode(UiCode code) {
        return code.equals(attribute.getCode());
    }

    protected static EditText createQualifierInput(Context context, String text, final Runnable onChangeHandler) {
        final EditText editText = new AppCompatEditText(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    onChangeHandler.run();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                    onChangeHandler.run();
                return false;
            }
        });
        editText.setText(text);
        editText.setSingleLine();
        editText.setHint(R.string.hint_code_qualifier_specify);
        return editText;
    }

    public static class CodeValue {
        String code;
        String qualifier;

        CodeValue(String code) {
            this(code, null);
        }

        CodeValue(String code, String qualifier) {
            this.code = code;
            this.qualifier = qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CodeValue codeValue = (CodeValue) o;

            return code.equals(codeValue.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }
    }
}


