package org.openforis.collect.android.gui;

import org.openforis.collect.android.viewmodel.*;

import java.util.HashSet;
import java.util.Set;

public class SmartNext {
    private final UiNode fromNode;
    private final Set<UiNode> branchesTried = new HashSet<UiNode>();

    public SmartNext(UiNode fromNode) {
        if (fromNode == null)
            throw new IllegalArgumentException("Node is null");
        this.fromNode = fromNode;
    }

    public UiNode next() {
        UiNode nodeToTry = fromNode;
        while (nodeToTry != null) {
            if (isNext(nodeToTry))
                return nodeToTry;
            nodeToTry = nextToTry(nodeToTry);
        }
        return null;
    }

    private UiNode nextToTry(UiNode node) {
        boolean branchTried = branchesTried.contains(node);
        branchesTried.add(node.getParent());

        if (hasChildren(node) && !branchTried)
            return firstChild(node);
        if (hasNextSibling(node))
            return nextSibling(node);
        if (node.getParent() != null)
            return nextParent(node);
        return null;
    }

    private UiInternalNode nextParent(UiNode node) {
        UiInternalNode parent = node.getParent();
        if (parent.getParent() == null || !parent.getParent().excludeWhenNavigating())
            return parent;
        return parent.getParent();
    }

    private boolean hasNextSibling(UiNode node) {
        return node.getParent() != null && node.getIndexInParent() < node.getSiblingCount() - 1;
    }

    private UiNode firstChild(UiNode node) {
        return ((UiInternalNode) node).getFirstChild();
    }

    public boolean hasNext() {
        return next() != null;
    }

    private boolean hasChildren(UiNode node) {
        return node instanceof UiInternalNode && !node.excludeWhenNavigating() && !((UiInternalNode) node).getChildren().isEmpty();
    }

    private UiNode nextSibling(UiNode node) {
        return node.getSiblingAt(node.getIndexInParent() + 1);
    }

    public boolean isNext(UiNode node) {
        return node != fromNode && node.isRelevant() &&
                (node instanceof UiAttribute
                        || node instanceof UiAttributeCollection
                        || node instanceof UiEntityCollection);
    }
}
