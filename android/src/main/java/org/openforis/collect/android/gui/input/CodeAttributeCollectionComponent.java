package org.openforis.collect.android.gui.input;

import android.graphics.Paint;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.detail.CodeListDescriptionDialogFragment;
import org.openforis.collect.android.viewmodel.*;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public abstract class CodeAttributeCollectionComponent extends AttributeCollectionComponent {
    private static final String DESCRIPTION_BUTTON_TAG = "descriptionButton";
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
            return new NewCodeAttributeCheckBoxComponent(attributeCollection, codeListService, surveyService, context);
        return new AutoCompleteCodeAttributeCollectionComponent(attributeCollection, codeListService, surveyService, context);
    }

    private boolean containsDescription() {
        for (UiCode code : codeList.getCodes())
            if (StringUtils.isNotEmpty(code.getDescription()))
                return true;
        return false;
    }

    void initCodeList() {
        if (codeList == null || isCodeListRefreshForced()) {
            setCodeListRefreshForced(false);
            codeList = codeListService.codeList(attributeCollection);
            uiHandler.post(new Runnable() {
                public void run() {
                    if (containsDescription())
                        includeDescriptionsButton();
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

    private void includeDescriptionsButton() {
        View inputView = toInputView();
        ViewGroup parent = (ViewGroup) inputView.getParent();
        if (parent.findViewWithTag(DESCRIPTION_BUTTON_TAG) == null) {
            Button button = new AppCompatButton(context);
            button.setTextAppearance(context, android.R.style.TextAppearance_Small);
            button.setTag(DESCRIPTION_BUTTON_TAG);
            button.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setText("Show code descriptions");
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
}


