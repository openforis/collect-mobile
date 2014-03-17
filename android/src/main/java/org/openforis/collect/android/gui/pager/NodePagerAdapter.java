package org.openforis.collect.android.gui.pager;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import org.openforis.collect.android.gui.detail.NodeDetailFragment;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class NodePagerAdapter extends FragmentPagerAdapter {
    private final Map<UiNode, NodeDetailFragment> fragmentByNode;
    private final UiInternalNode pagerNode;

    public NodePagerAdapter(FragmentManager fragmentManager, Map<UiNode, NodeDetailFragment> fragmentByNode, UiInternalNode pagerNode) {
        super(fragmentManager);
        this.fragmentByNode = fragmentByNode;
        this.pagerNode = pagerNode;
    }

    public NodeDetailFragment getItem(int position) {
        UiNode node = pagerNode.getChildAt(position);
        return fragmentByNode.get(node);
    }

    public CharSequence getPageTitle(int position) {
        return pagerNode.getChildAt(position).getLabel();
    }

    public int getCount() {
        return pagerNode.getChildCount();
    }
}
