package org.openforis.collect.android.viewmodel;

/**
 * Contains placeholders for {@link UiRecord} as children. Keeping the actual instances here would use too much memory.
 *
 * @author Daniel Wiell
 */
public class UiRecordCollection extends UiInternalNode {
    public UiRecordCollection(int id, Definition definition) {
        super(id, true, definition);
        register(this);
    }

    public void setStatus(Status status) {
        // TODO: We ignore the status of the record collection initially - terribly ugly - fix!
    }

    public UiSurvey getUiSurvey() {
        return (UiSurvey) getParent();
    }

    public boolean excludeWhenNavigating() {
        return true;
    }
}
