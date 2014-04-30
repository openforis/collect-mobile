package org.openforis.collect.android.gui.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
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
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeChange;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class NodePagerFragment extends Fragment {
    private Map<UiNode, NodeDetailFragment> fragmentsByNode;
    private SurveyService surveyService;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        surveyService = ServiceLocator.surveyService();
        return inflater.inflate(R.layout.fragment_node_pager, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        fragmentsByNode = createDetailFragments();
        setupPager(view);
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

    public void onAttributeChange(UiAttribute attribute, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        for (NodeDetailFragment fragment : fragmentsByNode.values())
            fragment.onAttributeChange(attribute, attributeChanges);
    }

    private void setupPager(View view) {
        final ViewPager pager = (ViewPager) view.findViewById(R.id.attributePager);
        PagerAdapter pagerAdapter = new NodePagerAdapter(getChildFragmentManager(), fragmentsByNode, pagerNode());
        pager.setAdapter(pagerAdapter);

        final PageIndicator indicator = (PageIndicator) view.findViewById(R.id.attributePagerIndicator);
        indicator.setViewPager(pager);
        int selectedIndex = selectedNode().getIndexInParent();
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                UiNode selectedNode = surveyService.selectedNode().getSiblingAt(position);
                surveyService.selectNode(selectedNode.getId());
            }

            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = pager.getCurrentItem();
                    UiNode selectedNode = surveyService.selectedNode().getSiblingAt(position);
                    NodeDetailFragment detailFragment = fragmentsByNode.get(selectedNode);
                    if (detailFragment != null)
                        detailFragment.onSelected();
                }
            }
        };
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
}
