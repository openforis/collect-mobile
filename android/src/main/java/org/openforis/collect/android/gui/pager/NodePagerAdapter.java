package org.openforis.collect.android.gui.pager;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.openforis.collect.android.gui.detail.NodeDetailFragment;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class NodePagerAdapter extends FragmentPagerAdapter {
    private Map<UiNode, NodeDetailFragment> fragmentByNode;
    private final UiInternalNode pagerNode;

    public NodePagerAdapter(FragmentManager fragmentManager, Map<UiNode, NodeDetailFragment> fragmentByNode, UiInternalNode pagerNode) {
        super(fragmentManager);
        this.fragmentByNode = fragmentByNode;
        this.pagerNode = pagerNode;
    }

    @Override
    public NodeDetailFragment getItem(int position) {
        UiNode node = getRelevantChildAt(position);
        return fragmentByNode.get(node);
    }

    public CharSequence getPageTitle(int position) {
        UiNode node = getRelevantChildAt(position);
        return node.getLabel();
    }

    public int getCount() {
        return getRelevantChildren().size();
    }

    public int getItemPosition(Object object) {
        NodeDetailFragment fragment = (NodeDetailFragment) object;
        UiNode node = fragment.node();
        if (node.isRelevant()) {
            return pagerNode.getRelevantChildren().indexOf(node);
        } else {
            return POSITION_NONE; //forces view update when relevance changes
        }
    }

    private UiNode getRelevantChildAt(int position) {
        List<UiNode> relevantChildren = getRelevantChildren();
        return relevantChildren.get(position);
    }

    private List<UiNode> getRelevantChildren() {
        return pagerNode.getRelevantChildren();
    }
}
