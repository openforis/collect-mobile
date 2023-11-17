package org.openforis.collect.android.gui.input;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeComponent extends AttributeComponent<UiCodeAttribute> {
    static final int RADIO_GROUP_MAX_SIZE = 100;
    private UiCode parentCode;
    final CodeListService codeListService;
    final boolean enumerator;
    protected UiCodeList codeList;
    private boolean codeListRefreshForced;

    CodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        this.codeListService = codeListService;
        this.enumerator = attribute.getDefinition().isEnumerator();
    }

    public static CodeAttributeComponent create(UiCodeAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attribute);
        boolean enumerator = attribute.getDefinition().isEnumerator();
        if (maxCodeListSize <= RADIO_GROUP_MAX_SIZE && ! enumerator)
            return new RadioCodeAttributeComponent(attribute, codeListService, surveyService, context);
        return new AutoCompleteCodeAttributeComponent(attribute, codeListService, surveyService, context);
    }

    protected abstract UiCode selectedCode();

    public void onAttributeChange(UiAttribute changedAttribute) {
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

    private synchronized boolean isCodeListRefreshForced() {
        return codeListRefreshForced;
    }

    private synchronized void setCodeListRefreshForced(boolean codeListRefreshForced) {
        this.codeListRefreshForced = codeListRefreshForced;
    }

    @Override
    public boolean hasChanged() {
        if (codeList == null)
            return false;
        UiCode newCode = selectedCode();
        String newQualifier = qualifier(newCode);
        newQualifier = newQualifier == null ? "" : newQualifier;

        if (StringUtils.isNotEmpty(newQualifier) && newCode == null)
            newCode = codeList.getQualifiableCode();
        String oldQualifier = attribute.getQualifier() == null ? "" : attribute.getQualifier();

        return notEqual(attribute.getCode(), newCode)
                || notEqual(oldQualifier, newQualifier);
    }

    protected final boolean updateAttributeIfChanged() {
        if (codeList == null)
            return false;
        if (hasChanged()) {
            UiCode newCode = selectedCode();
            String newQualifier = qualifier(newCode);
            if (StringUtils.isNotEmpty(newQualifier) && newCode == null)
                newCode = codeList.getQualifiableCode();

            attribute.setCode(newCode);
            attribute.setQualifier(newQualifier);
            return true;
        }
        return false;
    }

    protected void initCodeList() {
        if (codeList == null || isCodeListRefreshForced()) {
            setCodeListRefreshForced(false);
            codeList = codeListService.codeList(attribute);
        }
    }

    private boolean hasChanged(UiCode newCode, String qualifier) {
        String oldQualifier = attribute.getQualifier() == null ? "" : attribute.getQualifier();
        String newQualifier = qualifier == null ? "" : qualifier;
        return notEqual(attribute.getCode(), newCode)
                || notEqual(oldQualifier, newQualifier);
    }

    protected abstract void initOptions();

    protected abstract String qualifier(UiCode selectedCode);

    final boolean isAttributeCode(UiCode code) {
        return code.equals(attribute.getCode());
    }

    protected static EditText createQualifierInput(final Activity context, String text, final Runnable onChangeHandler) {
        final EditText editText = new AppCompatEditText(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (AndroidVersion.greaterThan16() && !context.isDestroyed() && !hasFocus)
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

    protected static TextView createQualifierReadonlyText(Activity context, String text) {
        TextView textView = new TextView(context);
        textView.setTextSize(20);
        if (text != null) {
            textView.setText(text);
        }
        return textView;
    }
}


