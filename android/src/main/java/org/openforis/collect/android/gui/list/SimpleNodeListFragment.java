package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.List;

/**
 * @author Daniel Wiell
 * @author Stefano Ricci
 */
public class SimpleNodeListFragment extends Fragment {

    private SimpleNodeListAdapter listAdapter;
    private RecyclerView nodeListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_node_list, container, false);
        nodeListView = (RecyclerView) rootView.findViewById(R.id.node_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        nodeListView.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        nodeListView.setItemAnimator(itemAnimator);

        listAdapter = new SimpleNodeListAdapter(getActivity(), node().getParent(), new SimpleNodeListAdapter.OnItemClickListener() {
            public void onItemClick(int position, UiNode node) {
                if (nodeListView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    onAttributeSelected(position);
                }
            }
        });
        nodeListView.setAdapter(listAdapter);
        listAdapter.selectNode(node());
        return rootView;
    }

    private UiNode node() {
        return ServiceLocator.surveyService().selectedNode();
    }

    private void onAttributeSelected(int attributeIndex) {
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(attributeIndex);
    }

    public void notifyNodeChanged(UiNode node) {
        if (listAdapter.parentNode == node.getParent()) {
            List<UiNode> siblings = node.getParent().getChildren();
            List<UiNode> shownNodes = listAdapter.getNodes();
            int currentPosition = 0;
            for (UiNode sibling : siblings) {
                if (sibling.isRelevant()) {
                    if (shownNodes.contains(sibling)) {
                        listAdapter.notifyNodeChanged(node);
                    } else {
                        listAdapter.insert(currentPosition, sibling);
                    }
                    currentPosition++;
                } else {
                    listAdapter.remove(sibling);
                }
            }
        }
    }

    public void selectNode(UiNode node) {
        listAdapter.selectNode(node);
    }

    public void scrollToPosition(int position) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getActivity()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(position);
        nodeListView.getLayoutManager().startSmoothScroll(smoothScroller);
    }
}
