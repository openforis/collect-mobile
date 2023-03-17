package org.openforis.collect.android.viewmodel;

import org.openforis.collect.android.collectadapter.Definitions;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class UiInternalNode extends UiNode {
    // TODO: Use LinkedHashMap instead
    private final Map<Integer, UiNode> childById = new HashMap<Integer, UiNode>();
    private final List<UiNode> children = new ArrayList<UiNode>();

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

    @Override
    public Collection<UiNode> getChildrenByDefId(int childDefId) {
        Collection<UiNode> found = super.getChildrenByDefId(childDefId);
        for (UiNode childNode : children) {
            if (Definitions.extractOriginalDefinitionId(childNode.getDefinition()) == childDefId) {
                // single node definition
                found.add(childNode);
            } else {
                // multiple node definition
                found.addAll(childNode.getChildrenByDefId(childDefId));
            }
        }
        return found;
    }

    public UiNode getChildByDefId(int childDefId) {
        if (Definitions.extractOriginalDefinitionId(getDefinition()) == childDefId)
            return this;
        for (UiNode childNode : children) {
            if (Definitions.extractOriginalDefinitionId(childNode.getDefinition()) == childDefId) {
                return childNode;
            }
        }
        return null;
    }

    public List<UiNode> getRelevantChildren() {
        List<UiNode> result = new ArrayList<UiNode>(children);
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

    private void addChild(int position, UiNode node) {
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

    public UiNode getFirstEditableChild() {
        List<UiNode> relevantEditableChildren = getRelevantChildren();
        // filter out calculated and enumerator attributes
        CollectionUtils.filter(relevantEditableChildren, new Predicate<UiNode>() {
            public boolean evaluate(UiNode item) {
                Definition def = item.getDefinition();
                // if entity => true
                if (!(def instanceof UiAttributeDefinition)) return true;
                UiAttributeDefinition attrDef = (UiAttributeDefinition) def;
                // calculated attribute => false
                if (attrDef.calculated) return false;
                // enumerator attribute => false
                if ((def instanceof UiCodeAttributeDefinition && ((UiCodeAttributeDefinition) def).isEnumerator())) return false;
                // otherwise true
                return true;
            }
        });
        if (relevantEditableChildren.isEmpty()) {
            // throw new IllegalStateException("Node " + this + " contains no relevant children");
            return null;
        }
        return relevantEditableChildren.get(0);
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

    public void removeChild(UiNode node) {
        childById.remove(node.getId());
        children.remove(node);
        unregister(node);
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

}
