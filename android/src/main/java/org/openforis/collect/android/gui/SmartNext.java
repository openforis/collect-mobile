package org.openforis.collect.android.gui;

import org.openforis.collect.android.viewmodel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        List<UiNode> fullPath = fullNextNodePath();
        return fullPath.get(fullPath.size() - 1);
    }

    public List<UiNode> fullNextNodePath() {
        List<UiNode> result = new ArrayList<UiNode>();
        UiNode nodeToTry = fromNode;
        while (nodeToTry != null) {
            result.add(nodeToTry);
            if (isNext(nodeToTry))
                return result;
            nodeToTry = nextToTry(nodeToTry, true);
        }
        result.add(fromNode.getUiSurvey().getFirstChild()); // Back to record collection if no other
        return result;
    }

    private UiNode nextToTry(UiNode node, boolean includeNotRelevant) {
        boolean branchTried = branchesTried.contains(node);
        branchesTried.add(node.getParent());

        if (hasChildren(node) && !branchTried)
            return firstChild(node);
        if (hasNextSibling(node, includeNotRelevant))
            return nextSibling(node, includeNotRelevant);
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

    private boolean hasNextSibling(UiNode node, boolean includeNotRelevantNodes) {
        if (node.getParent() == null) {
            return false;
        } else if (includeNotRelevantNodes) {
            return node.getIndexInParent() < node.getSiblingCount() - 1;
        } else {
            List<UiNode> relevantSiblings = node.getRelevantSiblings();
            return relevantSiblings.indexOf(node) < relevantSiblings.size() - 1;
        }
    }

    private UiNode firstChild(UiNode node) {
        return ((UiInternalNode) node).getFirstChild();
    }

    public boolean hasNext() {
        return next() != fromNode;
    }

    private boolean hasChildren(UiNode node) {
        return node instanceof UiInternalNode && !node.excludeWhenNavigating() && !((UiInternalNode) node).getChildren().isEmpty();
    }

    private UiNode nextSibling(UiNode node, boolean includeNotRelevant) {
        if (includeNotRelevant) {
            return node.getSiblingAt(node.getIndexInParent() + 1);
        } else {
            List<UiNode> relevantSiblings = node.getRelevantSiblings();
            int nodeIndex = relevantSiblings.indexOf(node);
            return relevantSiblings.get(nodeIndex + 1);
        }
    }

    public boolean isNext(UiNode node) {
        return node != fromNode && node.isRelevant()
                && !node.isCalculated() &&
                (node instanceof UiAttribute
                        || node instanceof UiAttributeCollection
                        || node instanceof UiEntityCollection);
    }
}
