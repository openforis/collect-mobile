package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiEntityCollection extends UiInternalNode {
    private final int parentEntityId;

    public UiEntityCollection(int id, int parentEntityId, Definition definition) {
        super(id, definition);
        this.parentEntityId = parentEntityId;
    }

    public int getParentEntityId() {
        return parentEntityId;
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
