package org.openforis.collect.android.gui.pager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.detail.NodeDetailFragment;
import org.openforis.collect.android.viewmodel.UiNode;

/**
 * @author S. Ricci
 */
public class SimpleNodePagerAdapter extends FragmentStatePagerAdapter {

    private final FragmentManager fm;
    private NodeDetailFragment<?> nodeDetailFragment;

    public SimpleNodePagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    public Fragment getItem(int position) {
        switch(position) {
            case 1:
                return nodeDetailFragment;
            default:
                return new LoadingFragment();
        }
    }

    public int getCount() {
        return 3;
    }

    public int getItemPosition(Object object) {
        // this method will be called for every fragment in the ViewPager
        if (object instanceof LoadingFragment) {
            return POSITION_UNCHANGED; // don't force a reload
        } else {
            // POSITION_NONE means something like: this fragment is no longer valid
            // triggering the ViewPager to re-build the instance of this fragment.
            return POSITION_NONE;
        }
    }

    public void setCurrentNodeFragment(NodeDetailFragment<?> selectedFragment) {
        if (nodeDetailFragment != null) {
            nodeDetailFragment.onDeselect();
        }
        nodeDetailFragment = selectedFragment;
        nodeDetailFragment.onSelect();
        notifyDataSetChanged();
    }

    public void setCurrentNode(UiNode node) {
        setCurrentNodeFragment(NodeDetailFragment.create(node));
    }

    public NodeDetailFragment<?> getCurrentNodeDetailFragment() {
        return nodeDetailFragment;
    }

    public static class LoadingFragment extends Fragment {

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_loading, container, false);
        }
    }
}
