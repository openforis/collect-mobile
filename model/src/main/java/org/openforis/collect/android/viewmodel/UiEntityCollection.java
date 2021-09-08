package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiEntityCollection extends UiInternalNode implements UiNodeCollection {
    private final int parentEntityId;

    public UiEntityCollection(int id, int parentEntityId, boolean relevant, Definition definition) {
        super(id, relevant, definition);
        this.parentEntityId = parentEntityId;
    }

    public int getParentEntityId() {
        return parentEntityId;
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
