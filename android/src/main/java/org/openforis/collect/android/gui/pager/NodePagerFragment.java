package org.openforis.collect.android.gui.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.PageIndicator;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.detail.NodeDetailFragment;
import org.openforis.collect.android.gui.detail.NodePathDetailsFragment;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class NodePagerFragment extends Fragment {
    private Map<UiNode, NodeDetailFragment> fragmentsByNode;
    private SurveyService surveyService;
    private NodePathDetailsFragment nodePathDetailsFragment;
    private NodePagerAdapter pagerAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        surveyService = ServiceLocator.surveyService();
        return inflater.inflate(R.layout.fragment_node_pager, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentsByNode = createDetailFragments();
        setupPager(view);
        setupNodePathDetails();
    }

    private void setupNodePathDetails() {
        nodePathDetailsFragment = new NodePathDetailsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.node_path_details_container, nodePathDetailsFragment, "nodePathDetailsFragment")
                .commit();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        NodeDetailFragment selectedFragment = selectedFragment();
        if (selectedFragment == null)
            return;
        selectedFragment.onPrepareOptionsMenu(menu);
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private Map<UiNode, NodeDetailFragment> createDetailFragments() {
        HashMap<UiNode, NodeDetailFragment> fragmentsByNode = new LinkedHashMap<UiNode, NodeDetailFragment>();
        UiInternalNode selectedNode = pagerNode();
        for (UiNode uiNode : selectedNode.getChildren())
            fragmentsByNode.put(uiNode, NodeDetailFragment.create(uiNode));
        return fragmentsByNode;
    }

    public void onNodeSelected(UiNode previous, UiNode selected) {
        NodeDetailFragment deselectedFragment = fragmentsByNode.get(previous);
        if (deselectedFragment != null)
            deselectedFragment.onDeselect();

        NodeDetailFragment selectedFragment = fragmentsByNode.get(selected);
        if (selectedFragment != null)
            selectedFragment.onSelect();
    }

    synchronized  public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        for (NodeDetailFragment fragment : fragmentsByNode.values()) {
            fragment.onNodeChange(node, nodeChanges);
        }
        if (! nodeChanges.isEmpty())
            pagerAdapter.notifyDataSetChanged();

        if (nodePathDetailsFragment != null)
            nodePathDetailsFragment.nodeChanged(node);
    }

    private void setupPager(View view) {
        final ViewPager pager = (ViewPager) view.findViewById(R.id.attributePager);
        this.pagerAdapter = new NodePagerAdapter(getChildFragmentManager(), fragmentsByNode, pagerNode());
        pager.setAdapter(pagerAdapter);

        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                UiNode selectedNode = surveyService.selectedNode();
                UiNode nextNode = selectedNode.getRelevantSiblingAt(position);
                surveyService.selectNode(nextNode.getId());
            }

            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    UiNode selectedNode = surveyService.selectedNode();
                    NodeDetailFragment detailFragment = fragmentsByNode.get(selectedNode);
                    if (detailFragment != null)
                        detailFragment.onSelected();
                }
            }
        };
        final PageIndicator indicator = (PageIndicator) view.findViewById(R.id.attributePagerIndicator);
        indicator.setViewPager(pager);
        List<UiNode> relevantSiblings = selectedNode().getRelevantSiblings();
        int selectedIndex = relevantSiblings.indexOf(selectedNode());
        indicator.setOnPageChangeListener(pageChangeListener);
        indicator.setCurrentItem(selectedIndex);

        fragmentsByNode.get(selectedNode()).onSelect();
    }

    private UiInternalNode pagerNode() {
        return selectedNode().getParent();
    }

    private UiNode selectedNode() {
        return surveyService.selectedNode();
    }

    private NodeDetailFragment selectedFragment() {
        return fragmentsByNode.get(surveyService.selectedNode());
    }

    public void prepareNodeDeselect(UiNode node) {
        NodeDetailFragment fragment = fragmentsByNode.get(node);
        fragment.onDeselect();
    }
}
