package org.openforis.collect.android.gui.list;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openforis.collect.android.gui.ServiceLocator;

/**
 * @author Daniel Wiell
 */
public class NodeListDialogFragment extends DialogFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView view = new RecyclerView(getActivity());
        view.setAdapter(new SimpleNodeListAdapter(getActivity(), ServiceLocator.surveyService().selectedNode().getParent()));
        /*
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAttributeSelected(position);
            }
        });
        */
        return view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Attributes");
        return dialog;
    }

    /*
    private void onAttributeSelected(int attributeIndex) {
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(attributeIndex);
        dismiss();
    }
    */
}
