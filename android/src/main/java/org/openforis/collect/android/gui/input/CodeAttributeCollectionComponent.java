package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeCollectionComponent extends AttributeCollectionComponent {

    private UiCode parentCode;
    private boolean codeListRefreshForced;
    final CodeListService codeListService;
    protected UiCodeList codeList;

    CodeAttributeCollectionComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, surveyService, context);
        this.codeListService = codeListService;
    }

    protected abstract void initOptions();

    protected abstract void resetSelection();

    public static CodeAttributeCollectionComponent create(UiAttributeCollection attributeCollection, SurveyService surveyService, FragmentActivity context) {
        CodeListService codeListService = ServiceLocator.codeListService();
        int maxCodeListSize = codeListService.getMaxCodeListSize(attributeCollection.getDefinition().attributeDefinition);
        if (maxCodeListSize <= CodeAttributeComponent.RADIO_GROUP_MAX_SIZE)
            return new CodeAttributeCollectionCheckBoxComponent(attributeCollection, codeListService, surveyService, context);
        return new CodeAttributeCollectionAutoCompleteComponent(attributeCollection, codeListService, surveyService, context);
    }

    void initCodeList() {
        if (codeList == null || isCodeListRefreshForced()) {
            setCodeListRefreshForced(false);
            codeList = codeListService.codeList(attributeCollection);
            uiHandler.post(new Runnable() {
                public void run() {
                    if (codeList.containsDescription())
                        CodeAttributeComponent.includeDescriptionsButtonToParent(context, toInputView());
                }
            });
        }
    }

    public final void onAttributeChange(UiAttribute changedAttribute) {
        if (codeListService.isParentCodeAttribute(changedAttribute, attributeCollection)) {
            UiCode newParentCode = ((UiCodeAttribute) changedAttribute).getCode();
            if (newParentCode == parentCode) return;
            if (newParentCode == null || !newParentCode.equals(parentCode)) {
                while (attributeCollection.getChildCount() > 0)
                    surveyService.deletedAttribute(attributeCollection.getChildAt(0).getId());
                parentCode = newParentCode;
                setCodeListRefreshForced(true);
                resetSelection();
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
}


