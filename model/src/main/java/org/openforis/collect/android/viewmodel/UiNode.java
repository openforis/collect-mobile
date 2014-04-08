package org.openforis.collect.android.viewmodel;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class UiNode {
    private final int id;
    private final Definition definition;
    private UiInternalNode parent;
    private Status status;

    public UiNode(int id, Definition definition) {
        this.id = id;
        this.definition = definition;
        status = Status.OK;
    }

    public final void init() {
        if (parent != null)
            parent.register(this);
        if (this instanceof UiInternalNode) {
            for (UiNode child : ((UiInternalNode) this).getChildren())
                child.init();
        }
    }

    public int getId() {
        return id;
    }

    public Definition getDefinition() {
        return definition;
    }

    public String getName() {
        return definition.name;
    }

    public String getLabel() {
        return definition.label;
    }

    public UiInternalNode getParent() {
        return parent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void updateStatusOfNodeAndParents(UiNode.Status status) {
        setStatus(status);
        updateStatusOfParents();
    }

    public void updateStatusOfParents() {
        UiInternalNode parentNode = getParent();
        if (parentNode == null)
            return;
        UiNode.Status newParentStatus = UiNode.Status.values()[0];
        for (UiNode child : parentNode.getChildren()) {
            if (child.getStatus().ordinal() > newParentStatus.ordinal())
                newParentStatus = child.getStatus();
        }
        if (newParentStatus != parentNode.getStatus())
            parentNode.updateStatusOfNodeAndParents(newParentStatus);
    }

    public int getSiblingCount() {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildCount();
    }

    public UiNode getSiblingAt(int index) {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildAt(index);
    }

    public UiRecord getUiRecord() {  // TODO: Can this be removed or moved? This doesn't make sense for UiRecordCollection and UiSurvey
        if (this instanceof UiRecord)
            return (UiRecord) this;
        if (parent == null)
            return null;
        return parent.getUiRecord();
    }

    public UiSurvey getUiSurvey() {
        return (UiSurvey) getUiRecord().getParent().getParent();
    }

    public int getIndexInParent() {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildIndex(id);
    }

    void setParent(UiInternalNode parent) {
        this.parent = parent;
    }

    public String toString() {
        return id + ": " + definition;
    }

    public boolean excludeWhenNavigating() {
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiNode uiNode = (UiNode) o;

        return id == uiNode.id;

    }

    public int hashCode() {
        return id;
    }

    public static enum Status {
        OK, EMPTY, VALIDATION_WARNING, VALIDATION_ERROR
    }
}
