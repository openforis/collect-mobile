package org.openforis.collect.android.gui.list;

import android.content.Context;
import org.openforis.collect.R;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.util.StringUtils;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.idm.model.BooleanAttribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class EntityListAdapter extends NodeListAdapter {
    private static final int MAX_ATTRIBUTES = 2;
    public static final int MAX_ATTRIBUTE_LABEL_LENGTH = 20;
    public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 20;

    public EntityListAdapter(Context context, UiInternalNode parentNode) {
        super(context, parentNode);
    }

    public String getText(UiNode node) {
        List<UiAttribute> attributes = getKeyAttributes(node);
        if (attributes.isEmpty())
            attributes = allChildAttributes((UiInternalNode) node);
        return toNodeLabel(attributes);
    }

    private List<UiAttribute> getKeyAttributes(UiNode child) {
        if (child instanceof UiEntity)
            return ((UiEntity) child).getKeyAttributes();
        if (child instanceof UiRecord.Placeholder)
            return ((UiRecord.Placeholder) child).getKeyAttributes();
        throw new IllegalStateException("Unexpected node type. Expected UiEntity or UiRecord.Placeholder, was " + child.getClass());
    }

    private List<UiAttribute> allChildAttributes(UiInternalNode node) {
        List<UiAttribute> attributes = new ArrayList<UiAttribute>();
        for (UiNode potentialAttribute : node.getChildren()) {
            if (potentialAttribute instanceof UiAttribute)
                attributes.add((UiAttribute) potentialAttribute);
            if (attributes.size() >= MAX_ATTRIBUTES)
                return attributes;
        }
        return attributes;
    }

    private String toNodeLabel(List<UiAttribute> attributes) {
        StringBuilder s = new StringBuilder();
        for (Iterator<UiAttribute> iterator = attributes.iterator(); iterator.hasNext(); ) {
            // TODO: Should assemble the attribute name/value manually,
            // to so "Unspecified" can be picked up from a resource, and different parts can be styled separately
            UiAttribute attribute = iterator.next();
            String value = attribute.valueAsString();
            value = value == null ? context.getResources().getString(R.string.label_unspecified) : value;
            s.append(StringUtils.ellipsisMiddle(attribute.getLabel(), MAX_ATTRIBUTE_LABEL_LENGTH)).append(": ")
                    .append(StringUtils.ellipsisMiddle(value, MAX_ATTRIBUTE_VALUE_LENGTH));
            if (iterator.hasNext())
                s.append('\n');
        }
        return s.toString();
    }
}
