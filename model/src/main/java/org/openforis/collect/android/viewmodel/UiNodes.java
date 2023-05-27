package org.openforis.collect.android.viewmodel;

import java.util.ArrayList;
import java.util.List;

public abstract class UiNodes {

    public static final int MAX_SUMMARY_ATTRIBUTES = 3;

    private UiNodes() {
    }

    public static List<UiAttribute> getSummaryAttributes(UiNode node) {
        List<UiAttribute> summaryAttributes = new ArrayList<UiAttribute>();
        summaryAttributes.addAll(getKeyAttributes(node));
        summaryAttributes.addAll(getChildrenAttributesShownInSummary(node));
        if (summaryAttributes.isEmpty())
            summaryAttributes = allChildAttributes(node);

        if (summaryAttributes.size() > MAX_SUMMARY_ATTRIBUTES) {
            return summaryAttributes.subList(0, MAX_SUMMARY_ATTRIBUTES);
        }
        return summaryAttributes;
    }

    private static List<UiAttribute> getKeyAttributes(UiNode child) {
        if (child instanceof UiEntity)
            return ((UiEntity) child).getKeyAttributes();
        if (child instanceof UiRecord.Placeholder)
            return ((UiRecord.Placeholder) child).getKeyAttributes();
        throw new IllegalStateException("Unexpected node type. Expected UiEntity or UiRecord.Placeholder, was " + child.getClass());
    }


    private static List<UiAttribute> allChildAttributes(UiNode node) {
        List<UiAttribute> attributes = new ArrayList<UiAttribute>();
        if (node instanceof UiInternalNode) {
            for (UiNode potentialAttribute : ((UiInternalNode) node).getChildren()) {
                if (potentialAttribute instanceof UiAttribute)
                    attributes.add((UiAttribute) potentialAttribute);
                if (attributes.size() >= MAX_SUMMARY_ATTRIBUTES)
                    return attributes;
            }
        }
        return attributes;
    }

    private static List<UiAttribute> getChildrenAttributesShownInSummary(UiNode node) {
        List<UiAttribute> attributes = new ArrayList<UiAttribute>();
        if (node instanceof UiInternalNode) {
            for (UiNode childNode : ((UiInternalNode) node).getChildren()) {
                if (childNode instanceof UiAttribute && ((UiAttributeDefinition) childNode.getDefinition()).isShowInSummary()) {
                    attributes.add((UiAttribute) childNode);
                }
            }
        }
        return attributes;
    }
}
