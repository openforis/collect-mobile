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
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeAttributeRadioComponent extends CodeAttributeComponent {

    private static final int LIST_DIVIDER_HEIGHT_DPS = 20;
    private static final int LIST_ITEM_HEIGHT_DPS = 50;
    private static final int QUALIFIER_ITEM_HEIGHT_DPS = 40;

    private final ListView listView;
    private CodesAdapter listAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    CodeAttributeRadioComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);

        listView = new ListView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        listView.setLayoutParams(layoutParams);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(Views.px(getContext(), LIST_DIVIDER_HEIGHT_DPS));
        initOptions();
    }

    @Override
    protected UiCode getSelectedCode() {
        return null;
    }

    @Override
    protected CodeValue getSelectedCodeValue() {
        Collection<CodeValue> selectedCodes = listAdapter.getSelectedCodes();
        return selectedCodes.isEmpty() ? null : listAdapter.getSelectedCodes().iterator().next();
    }

    @Override
    protected void initOptions() {
        executor.execute(new LoadCodesTask());
    }

    @Override
    protected View toInputView() {
        return listView;
    }

    private Set<CodeValue> getAttributeCodeValues() {
        Set<CodeValue> selectedCodes = new HashSet<CodeValue>();
        CodeValue value = attribute.isEmpty()
                ? null
                : new CodeValue(attribute.getCode().getValue(), attribute.getQualifier());
        if (value != null)
            selectedCodes.add(value);

        return selectedCodes;
    }

    private boolean isValueShown() {
        return attribute.getDefinition().isValueShown();
    }

    protected class LoadCodesTask implements Runnable {
        public void run() {
            initCodeList();

            listAdapter = new CodesAdapter(getContext(), getAttributeCodeValues(), codeList,
                    isValueShown(), true,
                    new Runnable() {
                        public void run() {
                            saveNode();
                        }
                    }
            );
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
