package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiEntitySingleDefinition;
import org.openforis.collect.android.viewmodel.UiInternalNode;
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
    public void onResume() {
        super.onResume();
        listAdapter.refreshNodes();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_node_list, container, false);
        nodeListView = rootView.findViewById(R.id.node_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        nodeListView.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        nodeListView.setItemAnimator(itemAnimator);

        listAdapter = new SimpleNodeListAdapter(getActivity(), node().getParent(), new SimpleNodeListAdapter.OnItemClickListener() {
            public void onItemClick(int position, UiNode node) {
                if (position >= 0 && nodeListView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    onNodeSelected(position);
                }
            }
        });
        nodeListView.setAdapter(listAdapter);

        selectNode(node());
        return rootView;
    }

    private UiNode node() {
        return ServiceLocator.surveyService().selectedNode();
    }

    private void onNodeSelected(int nodeIndex) {
        FragmentActivity activity = getActivity();
        ViewPager viewPager = activity.findViewById(R.id.attributePager);
        if (viewPager.getCurrentItem() != nodeIndex) {
            viewPager.setCurrentItem(nodeIndex);
            UiNode node = listAdapter.getItem(nodeIndex);
            ServiceLocator.surveyService().selectNode(node.getId());
            if (node.getDefinition() instanceof UiEntitySingleDefinition && activity instanceof SurveyNodeActivity) {
                ((SurveyNodeActivity) activity).smartNextAttribute(null);
            }
        }
    }

    public void notifyNodeChanged(UiNode node) {
        UiInternalNode parent = node.getParent();
        boolean insideCollection = parent instanceof UiAttributeCollection || parent instanceof UiEntityCollection;
        if (insideCollection) {
            parent = parent.getParent();
        }
        if (listAdapter.parentNode == parent) {
            List<UiNode> siblings = parent.getChildren();
            List<UiNode> shownNodes = listAdapter.getNodes();
            int currentPosition = 0;
            for (UiNode sibling : siblings) {
                if (sibling.isRelevant()) {
                    if (shownNodes.contains(sibling)) {
                        if (insideCollection) {
                            listAdapter.notifyNodeChanged(node.getParent());
                        } else {
                            listAdapter.notifyNodeChanged(node);
                        }
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
        if (node != null) {
            listAdapter.selectNode(node);
            List<UiNode> siblings = node.getRelevantSiblings();
            int index = siblings.indexOf(node);
            nodeListView.scrollToPosition(index);
        }
    }
}
