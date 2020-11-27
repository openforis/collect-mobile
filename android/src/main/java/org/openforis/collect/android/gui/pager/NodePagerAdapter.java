package org.openforis.collect.android.gui.pager;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.openforis.collect.android.gui.detail.NodeDetailFragment;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.commons.collection.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class NodePagerAdapter extends FragmentPagerAdapter {
    private Map<UiNode, NodeDetailFragment> fragmentByNode;
    private List<UiNode> visibleNodes = new ArrayList<UiNode>();
    private List<UiNode> previouslyVisibleNodes = new ArrayList<UiNode>();

    public NodePagerAdapter(FragmentManager fragmentManager, Map<UiNode, NodeDetailFragment> fragmentByNode) {
        super(fragmentManager);
        this.fragmentByNode = fragmentByNode;
    }

    @Override
    public NodeDetailFragment getItem(int position) {
        UiNode node = visibleNodes.get(position);
        return fragmentByNode.get(node);
    }

    @Override
    public long getItemId(int position) {
        UiNode node = visibleNodes.get(position);
        return node.getId();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        UiNode node = visibleNodes.get(position);
        return node.getLabel();
    }

    @Override
    public int getCount() {
        return visibleNodes.size();
    }

    @Override
    public int getItemPosition(Object object) {
        NodeDetailFragment fragment = (NodeDetailFragment) object;
        UiNode node = fragment.node();
        if (node.isRelevant()) {
            int currentPosition = visibleNodes.indexOf(node);
            int oldPosition = previouslyVisibleNodes.indexOf(node);
            if (currentPosition >= 0) {
                if (currentPosition == oldPosition) {
                    return POSITION_UNCHANGED;
                } else {
                    return currentPosition;
                }
            } else {
                return POSITION_NONE;
            }
        } else {
            return POSITION_NONE; //forces view update when relevance changes
        }
    }

    public void setVisibleNodes(List<UiNode> nodes) {
        List<Integer> newIds = CollectionUtils.project(nodes, "id");
        List<Integer> oldIds = CollectionUtils.project(this.visibleNodes, "id");
        if (!newIds.equals(oldIds)) {
            this.previouslyVisibleNodes = this.visibleNodes;
            this.visibleNodes = nodes;
            notifyDataSetChanged();
        }
    }
}
