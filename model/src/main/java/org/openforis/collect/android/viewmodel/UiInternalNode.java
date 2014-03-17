package org.openforis.collect.android.viewmodel;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class UiInternalNode extends UiNode {
    // TODO: Use LinkedHashMap instead
    private Map<Integer, UiNode> childById = new HashMap<Integer, UiNode>();
    private List<UiNode> children = new ArrayList<UiNode>();

    public UiInternalNode(int id, Definition definition) {
        super(id, definition);
    }

    public void register(UiNode node) {
        if (getParent() != null)
            getParent().register(node);
    }

    public List<UiNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(UiNode node) {
        children.add(node);
        childById.put(node.getId(), node);
        node.setParent(this);
    }

    public int getChildCount() {
        return children.size();
    }

    public UiNode getChildById(int id) {
        UiNode child = childById.get(id);
        if (child == null)
            throw new IllegalStateException("Node " + this + " contains no child with id " + id);
        return child;
    }

    public int getChildIndex(int id) {
        for (int i = 0; i < children.size(); i++) {
            UiNode child = children.get(i);
            if (child.getId() == id)
                return i;
        }
        throw new IllegalStateException("Node " + this + " contains no child with id " + id);
    }

    public UiNode getFirstChild() {
        if (children.isEmpty())
            throw new IllegalStateException("Node " + this + " contains no children");
        return children.get(0);
    }

    public UiNode getChildAt(int childIndex) {
        if (childIndex >= children.size())
            throw new IllegalStateException("Node " + this + " doesn't contain a child at index " + childIndex);
        return children.get(childIndex);
    }

    public void addChildren(Collection<? extends UiNode> nodes) {
        for (UiNode child : nodes)
            addChild(child);
    }

    public List<String> getChildrenLabels() {
        List<String> labels = new ArrayList<String>();
        for (UiNode child : children) {
            labels.add(child.getLabel());
        }
        return labels;
    }

    public void updateStatusOfNodeAndDescendants() {
        int statusOrdinal = determineChildrenMaxStatus();
        Status status = Status.values()[statusOrdinal];
        setStatus(status);
    }

    private int determineChildrenMaxStatus() {
        int status = 0;
        int maxStatus = Status.values().length - 1;
        for (UiNode child : children) {
            int childStatus;
            if (child instanceof UiInternalNode) {
                UiInternalNode internalNodeChild = (UiInternalNode) child;
                internalNodeChild.updateStatusOfNodeAndDescendants();
                childStatus = internalNodeChild.getStatus().ordinal();
            } else
                childStatus = child.getStatus().ordinal();
            if (childStatus > status) {
                status = childStatus;
                if (status == maxStatus)
                    return maxStatus;
            }
        }
        return status;
    }

    public String toString() {
        return getLabel();
    }
}
