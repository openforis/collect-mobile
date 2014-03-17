package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;

// TODO: Implement this...

/**
 * @author Daniel Wiell
 */
public class AttributeCollectionDetailFragment extends NodeDetailFragment<UiAttributeCollection> {
    public View createView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attribute_detail, container, false);
        ((TextView) rootView.findViewById(R.id.attribute_label)).setText(node().getLabel());
        TextView nodeLabel = (TextView) rootView.findViewById(R.id.attribute_label);
        nodeLabel.setText(node().getLabel());
        return rootView;
    }
}
