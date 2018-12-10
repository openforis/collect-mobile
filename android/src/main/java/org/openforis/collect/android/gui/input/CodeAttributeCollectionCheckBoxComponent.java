package org.openforis.collect.android.gui.input;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiValidationError;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeAttributeCollectionCheckBoxComponent extends CodeAttributeCollectionComponent {

    private static final int LIST_DIVIDER_HEIGHT_DPS = 20;
    private static final int LIST_ITEM_HEIGHT_DPS = 50;
    private static final int QUALIFIER_ITEM_HEIGHT_DPS = 40;

    protected ListView listView;
    protected CodeListViewAdapter listAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    CodeAttributeCollectionCheckBoxComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, codeListService, surveyService, context);

        listView = new ListView(getContext());
        listView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(Views.px(getContext(), LIST_DIVIDER_HEIGHT_DPS));

        initOptions();
    }

    @Override
    protected void initOptions() {
        executor.execute(new LoadCodesTask());
    }

    @Override
    protected void resetSelection() {
        listAdapter.resetSelectedCodes();
    }

    @Override
    protected View toInputView() {
        return listView;
    }

    @Override
    protected void resetValidationErrors() {

    }

    @Override
    protected void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors) {

    }

    @Override
    protected Set<UiAttribute> updateChangedAttributes() {
        return new HashSet<UiAttribute>();
    }

    protected Set<CodeAttributeComponent.CodeValue> getAttributeCodeValues() {
        List<UiNode> children = attributeCollection.getChildren();
        Set<CodeAttributeComponent.CodeValue> selectedCodes = new LinkedHashSet<CodeAttributeComponent.CodeValue>(children.size());
        for (UiNode child : children) {
            UiCodeAttribute childCode = (UiCodeAttribute) child;
            selectedCodes.add(new CodeAttributeComponent.CodeValue(
                    childCode.getCode().getValue(),
                    childCode.getQualifier())
            );
        }
        return selectedCodes;
    }

    protected UiCodeAttributeDefinition getCodeAttributeDefinition() {
        return (UiCodeAttributeDefinition) attributeCollection.getDefinition().getAttributeDefinition();
    }

    protected Set<CodeAttributeComponent.CodeValue> getSelectedCodeValues() {
        return listAdapter.getSelectedCodes();
    }

    protected boolean isValueShown() {
        return getCodeAttributeDefinition().isValueShown();
    }

    private class LoadCodesTask implements Runnable {
        public void run() {
            initCodeList();

            listAdapter = new CodeListViewAdapter(getContext(), getAttributeCodeValues(), codeList,
                    isValueShown(), false, new Runnable() {
                public void run() {
                    saveNode();
                }
            });
            listView.setAdapter(listAdapter);

            List<UiCode> qualifiableCodes = new ArrayList<UiCode>(codeList.getCodes());
            CollectionUtils.filter(qualifiableCodes, new Predicate<UiCode>() {
                public boolean evaluate(UiCode code) {
                    return codeList.isQualifiable(code);
                }
            });
            listView.getLayoutParams().height = Views.px(getContext(),
                    codeList.getCodes().size() * LIST_ITEM_HEIGHT_DPS +
                            qualifiableCodes.size() * QUALIFIER_ITEM_HEIGHT_DPS
            );
        }
    }
}
