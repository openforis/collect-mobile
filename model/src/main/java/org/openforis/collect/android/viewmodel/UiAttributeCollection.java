package org.openforis.collect.android.viewmodel;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public class UiAttributeCollection extends UiInternalNode {
    private final int parentEntityId;

    public UiAttributeCollection(int id, int parentEntityId, UiAttributeCollectionDefinition definition) {
        super(id, definition);
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
