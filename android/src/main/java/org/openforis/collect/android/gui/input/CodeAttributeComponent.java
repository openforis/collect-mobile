package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeComponent extends AttributeComponent<UiCodeAttribute> {
    public static final int RADIO_GROUP_MAX_SIZE = 20;
    private UiCode parentCode;
    protected final CodeListService codeListService;
    protected UiCodeList codeList;

    protected CodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        this.codeListService = codeListService;
    }

    public static CodeAttributeComponent create(UiCodeAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attribute);
        if (maxCodeListSize <= RADIO_GROUP_MAX_SIZE)
            return new RadioCodeAttributeComponent(attribute, codeListService, surveyService, context);
        return new AutoCompleteCodeAttributeComponent(attribute, codeListService, surveyService, context);
    }

    protected abstract UiCode selectedCode();

    public final void onAttributeChange(UiAttribute changedAttribute) {
        if (changedAttribute != attribute && codeListService.isParentCodeAttribute(changedAttribute, attribute)) {
            UiCode newParentCode = ((UiCodeAttribute) changedAttribute).getCode();
            if (newParentCode == parentCode) return;
            if (newParentCode == null || !newParentCode.equals(parentCode)) {
                parentCode = newParentCode;
                initOptions();
            }
        }
    }

    protected final boolean updateAttributeIfChanged() {
        if (codeList == null)
            return false;
        UiCode newCode = selectedCode();
        String newQualifier = qualifier(newCode);
        if (hasChanged(newCode, newQualifier)) {
            attribute.setCode(newCode);
            attribute.setQualifier(newQualifier);
            return true;
        }
        return false;
    }

    protected void initCodeList() {
        codeList = codeListService.codeList(attribute);
    }

    private boolean hasChanged(UiCode newCode, String qualifier) {
        return notEqual(attribute.getCode(), newCode)
                || notEqual(attribute.getQualifier(), qualifier);
    }

    protected abstract void initOptions();

    protected abstract String qualifier(UiCode selectedCode);

    protected final boolean isAttributeCode(UiCode code) {
        return code.equals(attribute.getCode());
    }
}


