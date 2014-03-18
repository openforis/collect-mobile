package org.openforis.collect.android.gui.list;

import android.content.Context;
import org.openforis.collect.android.viewmodel.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class EntityListAdapter extends NodeListAdapter {
    private static final int MAX_ATTRIBUTES = 2;
    private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 50;

    public EntityListAdapter(Context context, UiInternalNode parentNode) {
        super(context, parentNode);
    }

    public String getText(UiNode node) {
        // TODO: Should assemble the attribute name/value manually,
        // to so "Unspecified" can be picked up from a resource, and different parts can be styled separately
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
            String attribute = iterator.next().toString();
            s.append(attribute.substring(0, Math.min(attribute.length(), MAX_ATTRIBUTE_VALUE_LENGTH)));
            if (iterator.hasNext())
                s.append('\n');
        }
        return s.toString();
    }
}
