package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.idm.model.Code;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCodeAttributeRadioComponent extends CodeAttributeComponent {

    private final LinearLayout layout;
    private final ListView listView;
    private CodesAdapter listAdapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    NewCodeAttributeRadioComponent(UiCodeAttribute attribute, CodeListService codeListService, SurveyService surveyService, FragmentActivity context) {
        super(attribute, codeListService, surveyService, context);
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        listView = new ListView(getContext());

        initOptions();

        layout.addView(listView);
    }

    @Override
    protected Code selectedCode2() {
        Collection<Code> selectedCodes = listAdapter.getSelectedCodes();
        return selectedCodes.isEmpty() ? null : listAdapter.getSelectedCodes().iterator().next();
    }

    @Override
    protected void initOptions() {
        executor.execute(new LoadCodesTask());
    }

    @Override
    protected String qualifier(UiCode selectedCode) {
        return null;
    }

    @Override
    protected View toInputView() {
        return layout;
    }

    private class LoadCodesTask implements Runnable {
        public void run() {
            initCodeList();
            Set<Code> selectedCodes = new HashSet<Code>();
            Code code = attribute.isEmpty() ? null : new Code(attribute.getCode().getValue(), attribute.getQualifier());
            if (code != null)
                selectedCodes.add(code);
            listAdapter = new CodesAdapter(getContext(), selectedCodes, codeList, attribute.getDefinition().isValueShown());
            listView.setAdapter(listAdapter);
        }
    }
}
