package org.openforis.collect.android.viewmodelmanager;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface NodeRepository {

    void insert(List<NodeDto> nodes, Map<Integer, StatusChange> statusChanges);

    NodeDto.Collection recordNodes(int recordId);

    void update(NodeDto node, Map<Integer, StatusChange> statusChanges);

    NodeDto.Collection surveyRecords(int surveyId);

    void removeAll(List<Integer> ids, Map<Integer, StatusChange> statusChanges);

    void removeRecord(int recordId);
}
