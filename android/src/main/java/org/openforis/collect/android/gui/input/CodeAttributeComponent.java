package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeComponent extends AttributeComponent<UiCodeAttribute> {
    private static final int RADIO_GROUP_MAX_SIZE = 20;
    private UiCode parentCode;

    protected CodeAttributeComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        this.codeListService = codeListService;
    }

    protected final CodeListService codeListService;

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
        UiCode newCode = selectedCode();
        if (hasChanged(newCode)) {
            attribute.setCode(newCode);
            notifyAboutAttributeChange();
            return true;
        }
        return false;
    }

    private boolean hasChanged(UiCode newCode) {
        UiCode oldCode = attribute.getCode();
        if (oldCode == null)
            return newCode != null;
        return !oldCode.equals(newCode);
    }

    protected abstract void initOptions();

    protected final boolean isAttributeCode(UiCode code) {
        return code.equals(attribute.getCode());
    }
}


