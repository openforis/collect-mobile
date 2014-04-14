package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class TextCollectionComponent extends AttributeCollectionInputComponent<UiTextAttribute> {
    private final LinearLayout view;
    private final View rootView;
    private final UiAttributeCollection attributeCollection;
    private final List<AttributeViewHolder> attributeViewHolders = new ArrayList<AttributeViewHolder>();

    public TextCollectionComponent(final UiAttributeCollection attributeCollection, LayoutInflater inflater, Context context) {
        super(inflater, context);
        this.attributeCollection = attributeCollection;
        rootView = inflate(R.layout.attribute_collection_detail);
        final View addAttribute = rootView.findViewById(R.id.action_add_node);
        addAttribute.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UiTextAttribute attribute = (UiTextAttribute) surveyService.addAttribute();
                addInputForAttribute(attribute);
            }
        });

        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        for (UiNode child : attributeCollection.getChildren()) {
            UiTextAttribute attribute = (UiTextAttribute) child;
            addInputForAttribute(attribute);
        }
    }

    private void addInputForAttribute(UiTextAttribute attribute) {
        // TODO: Focus, keyboard, save attribute when focus is lost etc
        AttributeViewHolder viewHolder = new AttributeViewHolder(context(), attribute);
        attributeViewHolders.add(viewHolder);
        view.addView(viewHolder.view);
    }

    public View getRootView() {
        return rootView;
    }

    public View getView() {
        return view;
    }

    public void updateAttributeCollection() {
        List<UiNode> children = attributeCollection.getChildren();
        for (int i = 0; i < children.size(); i++) {
            UiNode child = children.get(i);
            UiTextAttribute attribute = (UiTextAttribute) child;
            attribute.setText(attributeViewHolders.get(i).text());
            addInputForAttribute(attribute);
        }
        surveyService.updateAttributeCollection(attributeCollection);
    }

    private static class AttributeViewHolder {
        final TextView view;

        AttributeViewHolder(Context context, UiTextAttribute attribute) {
            TextView textView = new EditText(context);
            textView.setText(attribute.getText());
            this.view = textView;
        }

        String text() {
            return view.getText().toString();
        }
    }
}
