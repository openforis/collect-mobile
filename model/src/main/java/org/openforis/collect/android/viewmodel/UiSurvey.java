package org.openforis.collect.android.viewmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class UiSurvey extends UiInternalNode {
    private final Map<String, UiRecordCollection> recordCollectionByName = new HashMap<String, UiRecordCollection>();
    private final Map<Integer, UiRecordCollection> recordCollectionById = new HashMap<Integer, UiRecordCollection>();

    public UiSurvey(int id, Definition definition) {
        super(id, definition);
    }

    public void addChild(UiNode node) {
        super.addChild(node);
        recordCollectionByName.put(node.getName(), (UiRecordCollection) node);
        recordCollectionById.put(node.getId(), (UiRecordCollection) node);
    }

    public void addRecord(UiRecord record) {
        UiRecordCollection recordCollection = lookupRecordCollection(record.getName());
        if (recordCollection == null)
            throw new IllegalStateException("No record collection with name " + record.getName());
        recordCollection.addChild(record.createPlaceholder());
    }

    public UiRecordCollection lookupRecordCollection(String name) {
        return recordCollectionByName.get(name);
    }

    public UiRecordCollection lookupRecordCollection(int id) {
        return recordCollectionById.get(id);
    }
}
