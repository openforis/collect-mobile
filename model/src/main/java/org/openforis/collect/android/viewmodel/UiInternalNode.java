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

    public boolean isRelevant() { // TODO: Not intuitive, should be done elsewhere, so GUI can be notified
        boolean relevant = super.isRelevant();
        if (relevant && !children.isEmpty()) { // Is relevant only if there is a relevant child
            for (UiNode child : children)
                if (child.isRelevant())
                    return true;
            return false;
        }
        return relevant;
    }

    public void register(UiNode node) {
        if (getParent() != null)
            getParent().register(node);
    }

    public void unregister(UiNode node) {
        if (getParent() != null)
            getParent().unregister(node);
    }

    public List<UiNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(UiNode node) {
        addChild(children.size(), node);
    }

    public void addChild(int position, UiNode node) {
        children.add(position, node);
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

    public boolean containsChildWithId(int id) {
        return childById.containsKey(id);
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

    public void updateStatusOfNodeAndDescendants() {
        int maxStatus = 0;
        for (UiNode child : children) {
            int childStatus;
            if (child instanceof UiInternalNode) {
                UiInternalNode internalNodeChild = (UiInternalNode) child;
                internalNodeChild.updateStatusOfNodeAndDescendants();
                childStatus = internalNodeChild.getStatus().ordinal();
            } else
                childStatus = child.getStatus().ordinal();
            if (childStatus > maxStatus) {
                maxStatus = childStatus;
            }
        }
        setStatus(Status.values()[maxStatus]);
    }

    public String toString() {
        return getLabel();
    }

    public void removeChild(UiNode node) {
        childById.remove(node.getId());
        children.remove(node);
        unregister(node);
    }
}
