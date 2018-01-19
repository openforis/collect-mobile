package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiNode;

/**
 * @author Daniel Wiell
 * @author Stefano Ricci
 */
public class NodeListDialogFragment extends DialogFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_node_list, container, false);
        rootView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.node_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        UiNode selectedNode = ServiceLocator.surveyService().selectedNode();

        RecyclerView.Adapter adapter = new SimpleNodeListAdapter(getActivity(), selectedNode.getParent(),
                new SimpleNodeListAdapter.OnItemClickListener() {
            public void onItemClick(int position, UiNode node) {
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    onNodeSelected(position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private void onNodeSelected(int nodeIndex) {
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(nodeIndex);
        dismiss();
    }
}
