package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiAttributeCollection extends UiInternalNode {

    public UiAttributeCollection(int id, Definition definition) {
        super(id, definition);
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
