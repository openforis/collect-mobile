package org.openforis.collect.android.gui.entitytable;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiNode;

public class EntityTableDialogFragment extends DialogFragment {
    private UiNode selectedNode;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedNode = ServiceLocator.surveyService().selectedNode();
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private boolean hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entity_table, container);
        TableFixHeaders table = (TableFixHeaders) view.findViewById(R.id.entity_table);
        NodeMatrixTableAdapter adapter = new NodeMatrixTableAdapter(selectedNode, getActivity());
        table.setAdapter(adapter);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        hideKeyboard();
    }

    public static void show(FragmentManager fragmentManager) {
        new EntityTableDialogFragment().show(fragmentManager, "entityTable");
    }
}
