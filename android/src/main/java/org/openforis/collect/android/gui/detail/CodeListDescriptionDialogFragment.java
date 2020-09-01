package org.openforis.collect.android.gui.detail;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;
import org.openforis.collect.android.viewmodel.UiNode;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CodeListDescriptionDialogFragment extends DialogFragment {
    private UiCodeList codeList;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        UiNode selectedNode = ServiceLocator.surveyService().selectedNode();
        CodeListService codeListService = ServiceLocator.codeListService();
        if (selectedNode instanceof UiCodeAttribute)
            codeList = codeListService.codeList(((UiCodeAttribute) selectedNode));
        else if (selectedNode instanceof UiAttributeCollection)
            codeList = codeListService.codeList((UiAttributeCollection) selectedNode);
        else
            throw new IllegalStateException("Opening code list description for invalid node: " + selectedNode);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.code_item_descriptions, container, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.code_item_descriptions);

        for (UiCode code : codeList.getCodes()) {
            TableRow tableRow = new TableRow(getActivity());
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(WRAP_CONTENT, 0, 1);
            rowParams.gravity = Gravity.CENTER_VERTICAL;

            TextView codeView = new TextView(getActivity());
            codeView.setText(code.toString());
            styleCell(codeView, 0.3f);
            tableRow.addView(codeView);

            TextView descriptionView = new TextView(getActivity());
            descriptionView.setText(code.getDescription());
            styleCell(descriptionView, 0.7f);
            tableRow.addView(descriptionView);

            tableLayout.addView(tableRow, rowParams);
        }

        ImageButton closeButton = (ImageButton) view.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }


    private void styleCell(TextView textView, float weight) {
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, WRAP_CONTENT);
        params.weight = weight;
        textView.setLayoutParams(params);
        textView.setPadding(8, 16, 8, 0);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        Keyboard.hide(getActivity());
        super.onViewCreated(view, savedInstanceState);
    }

    public static void show(FragmentManager fragmentManager) {
        new CodeListDescriptionDialogFragment().show(fragmentManager, "codeListDescription");
    }
}
