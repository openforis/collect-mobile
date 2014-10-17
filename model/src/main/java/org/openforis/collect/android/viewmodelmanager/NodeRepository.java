package org.openforis.collect.android.viewmodelmanager;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface NodeRepository {

    void insert(List<NodeDto> nodes);

    NodeDto.Collection recordNodes(int recordId);

    void update(NodeDto node, List<Map<String, Object>> statusChanges, String recordStatus);

    NodeDto.Collection surveyRecords(int surveyId);

    void removeAll(List<Integer> ids, List<Map<String, Object>> statusChanges, NodeDto recordToUpdateStatusFor);

    void removeRecord(int recordId);
}
