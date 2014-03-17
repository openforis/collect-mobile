package org.openforis.collect.android.viewmodel;

/**
 * Contains placeholders for {@link UiRecord} as children. Keeping the actual instances here would use too much memory.
 *
 * @author Daniel Wiell
 */
public class UiRecordCollection extends UiInternalNode {
    public UiRecordCollection(int id, Definition definition) {
        super(id, definition);
        register(this);
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
