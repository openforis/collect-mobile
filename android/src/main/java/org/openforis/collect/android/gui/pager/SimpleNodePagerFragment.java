package org.openforis.collect.android.gui.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.android.internal.util.Predicate;
import com.viewpagerindicator.PageIndicator;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.detail.NodePathDetailsFragment;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

public class SimpleNodePagerFragment extends Fragment {

    private SurveyService surveyService;
    private NodePathDetailsFragment nodePathDetailsFragment;
    private SimpleNodePagerAdapter pagerAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        surveyService = ServiceLocator.surveyService();
        return inflater.inflate(R.layout.fragment_node_pager, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
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
        if (pagerAdapter.getCurrentNodeDetailFragment() == null)
            return;
        pagerAdapter.getCurrentNodeDetailFragment().onPrepareOptionsMenu(menu);
    }

    public void onNodeSelected(UiNode previous, UiNode selected) {pagerAdapter.setCurrentNode(selected);
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        /*
        for (NodeDetailFragment fragment : fragmentsByNode.values())
            fragment.onNodeChange(node, nodeChanges);

        if (nodePathDetailsFragment != null)
            nodePathDetailsFragment.nodeChanged(node);
        */
    }

    private void setupPager(View view) {
        final ViewPager pager = (ViewPager) view.findViewById(R.id.attributePager);

        pagerAdapter = new SimpleNodePagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        final PageIndicator indicator = (PageIndicator) view.findViewById(R.id.attributePagerIndicator);
        indicator.setViewPager(pager);
        int selectedIndex = selectedNode().getIndexInParent();
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                UiNode currentNode = surveyService.selectedNode();
                final UiNode nextNode = determineNextNode(position);
                if (nextNode != currentNode) {
                    if (nextNode.getDefinition().relevanceSources.contains(currentNode.getDefinition())) {
                        Dialogs.showProgressDialogWhile(getActivity(), new Predicate<Void>() {
                            public boolean apply(Void aVoid) {
                                return surveyService.isUpdating();
                            }
                        }, new Runnable() {
                            public void run() {
                                showNextNode(pager, nextNode);
                            }
                        });
                    } else {
                        showNextNode(pager, nextNode);
                    }
                }
            }

            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    UiNode selectedNode = surveyService.selectedNode();
                    pagerAdapter.setCurrentNode(selectedNode);
                    pager.setCurrentItem(1);
                }
            }
        };
        indicator.setOnPageChangeListener(pageChangeListener);
        indicator.setCurrentItem(selectedIndex);

        pagerAdapter.setCurrentNode(selectedNode());
        pager.setCurrentItem(1);
    }

    private UiNode determineNextNode(int pagePosition) {
        UiNode currentNode = surveyService.selectedNode();
        int currentNodeIndex = currentNode.getIndexInParent();
        int nextNodeIndex;
        switch(pagePosition) {
            case 0:
                nextNodeIndex = currentNodeIndex - 1;
                break;
            case 2:
                nextNodeIndex = currentNodeIndex + 1;
                break;
            default:
                nextNodeIndex = currentNodeIndex;
        }
        if (nextNodeIndex != currentNodeIndex && nextNodeIndex >= 0 && nextNodeIndex < currentNode.getSiblingCount()){
            UiNode nextNode=currentNode.getSiblingAt(nextNodeIndex);
            return nextNode;
        } else {
            return currentNode;
        }
    }

    private void showNextNode(ViewPager pager, UiNode node) {
        surveyService.selectNode(node.getId());
        pagerAdapter.setCurrentNode(node);
        pager.setCurrentItem(1);
    }

    private UiNode selectedNode() {
        return surveyService.selectedNode();
    }

}
