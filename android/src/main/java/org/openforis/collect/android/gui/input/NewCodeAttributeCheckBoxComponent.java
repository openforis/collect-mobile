package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiAttributeCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCodeAttributeCheckBoxComponent extends CodeAttributeCollectionComponent {

    private final LinearLayout layout;
    private final ListView listView;
    private CodeAttributeComponent.CodesAdapter listAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    NewCodeAttributeCheckBoxComponent(UiAttributeCollection attributeCollection, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attributeCollection, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        listView = new ListView(getContext());

        initOptions();

        layout.addView(listView);
    }

    @Override
    protected void initOptions() {
        executor.execute(new LoadCodesTask());
    }

    @Override
    protected void resetSelection() {

    }

    @Override
    protected View toInputView() {
        return layout;
    }

    @Override
    protected void resetValidationErrors() {

    }

    @Override
    protected void setValidationError(UiAttribute attribute, Set<UiValidationError> validationErrors) {

    }

    @Override
    protected Set<UiAttribute> updateChangedAttributes() {
        return null;
    }

    private class LoadCodesTask implements Runnable {
        public void run() {
            initCodeList();

            boolean valueShown = ((UiCodeAttributeDefinition) attributeCollection.getDefinition().getAttributeDefinition()).isValueShown();

            listAdapter = new CodeAttributeComponent.CodesAdapter(getContext(), null, codeList,
                    valueShown, new Runnable() {
                public void run() {
                    saveNode();
                }
            });
            listView.setAdapter(listAdapter);
        }
    }
}
