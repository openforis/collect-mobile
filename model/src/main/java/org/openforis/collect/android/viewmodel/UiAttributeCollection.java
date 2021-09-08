package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiAttributeCollection extends UiInternalNode implements UiNodeCollection {
    private final int parentEntityId;

    public UiAttributeCollection(int id, int parentEntityId, boolean relevant, UiAttributeCollectionDefinition definition) {
        super(id, relevant, definition);
        this.parentEntityId = parentEntityId;
    }

    public int getParentEntityId() {
        return parentEntityId;
    }

    public UiAttributeCollectionDefinition getDefinition() {
        return (UiAttributeCollectionDefinition) super.getDefinition();
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
