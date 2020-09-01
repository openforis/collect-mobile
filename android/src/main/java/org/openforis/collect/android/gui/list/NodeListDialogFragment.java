package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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

        SimpleNodeListAdapter adapter = new SimpleNodeListAdapter(getActivity(), selectedNode.getParent(),
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
        ViewPager viewPager = getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(nodeIndex);
        dismiss();
    }
}
