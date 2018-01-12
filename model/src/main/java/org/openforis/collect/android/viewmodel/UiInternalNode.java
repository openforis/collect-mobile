package org.openforis.collect.android.viewmodel;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class UiInternalNode extends UiNode {
    // TODO: Use LinkedHashMap instead
    private Map<Integer, UiNode> childById = new HashMap<Integer, UiNode>();
    private List<UiNode> children = new ArrayList<UiNode>();

    public UiInternalNode(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public void register(UiNode node) {
        if (getParent() != null)
            getParent().register(node);
    }

    public void unregister(UiNode node) {
        if (getParent() != null)
            getParent().unregister(node);
    }

    public boolean isRelevant() {
        boolean isTab = getClass().equals(UiInternalNode.class);
        if (!isTab) // If not a tab, use relevance as specified
            return super.isRelevant();
        for (UiNode child : children)  // Tabs are relevant if any child is relevant
            if (child.isRelevant())
                return true;
        return false;
    }

    public List<UiNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<UiNode> getRelevantChildren() {
        List<UiNode> result = new ArrayList(children);
        CollectionUtils.filter(result, new Predicate<UiNode>() {
            public boolean evaluate(UiNode node) {
                return node.isRelevant();
            }
        });
        return result;
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

    public Status determineStatus(Set<UiValidationError> validationErrors) {
        Status status = super.determineStatus(validationErrors);
        for (UiNode child : children)
            if (child.getStatus().isWorseThen(status))
                status = child.getStatus();
        return status;
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
            if (child.isRelevant() && childStatus > maxStatus) {
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
