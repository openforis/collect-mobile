package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiInternalNode;

/**
 * @author Daniel Wiell
 */
public class InternalNodeDetailFragment extends NodeDetailFragment<UiInternalNode> {
    public View createView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_internal_node_detail, container, false);
    }


}
