package org.openforis.collect.android.gui.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
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
        String text;
        if (attribute instanceof UiCodeAttribute) {
            CodeListService codeListService = ServiceLocator.codeListService();
            UiCode codeItem = codeListService.codeListItem((UiCodeAttribute) attribute);
            text = codeItem == null ? null : codeItem.toString();
        } else {
            text = attribute.valueAsString();
        }
        viewHolder.value.setText(text);
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        super.onNodeChange(node, nodeChanges);
        if (node.equals(attribute))
            updateViewWithAttributeValue();
    }

    public static class ViewHolder {
        TextView value;

        public ViewHolder(Context context) {
            value = new AppCompatTextView(context);
            value.setTextSize(20);
        }
    }
}
