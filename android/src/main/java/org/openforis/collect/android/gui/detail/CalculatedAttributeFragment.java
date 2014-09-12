package org.openforis.collect.android.gui.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class CalculatedAttributeFragment<T extends UiNode> extends NodeDetailFragment<T> {
    private UiAttribute attribute;
    private ViewHolder viewHolder;

    protected final View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        attribute = (UiAttribute) node();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_calculated_attribute_detail, container, false);
        viewHolder = new ViewHolder(getActivity());
        updateViewWithAttributeValue();

        ViewGroup valueContainer = (ViewGroup) view.findViewById(R.id.calculated_attribute_container);
        valueContainer.addView(viewHolder.value);

        return view;
    }

    private void updateViewWithAttributeValue() {
        viewHolder.value.setText(attribute.valueAsString());
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        super.onNodeChange(node, nodeChanges);
        if (node.equals(attribute))
            updateViewWithAttributeValue();
    }

    public static class ViewHolder {
        TextView value;

        public ViewHolder(Context context) {
            value = new TextView(context);
        }
    }
}
