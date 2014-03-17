package org.openforis.collect.android.viewmodel;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public abstract class UiNode {
    private final int id;
    private final Definition definition;
    private UiInternalNode parent;

    public UiNode(int id, Definition definition) {
        this.id = id;
        this.definition = definition;
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

    public List<String> getSiblingLabels() {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildrenLabels();
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
}
