package org.openforis.collect.android.viewmodelmanager;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface NodeRepository {

    boolean insert(List<NodeDto> nodes, Map<Integer, StatusChange> statusChanges);

    NodeDto.Collection recordNodes(int recordId);

    boolean update(NodeDto node, Map<Integer, StatusChange> statusChanges);

    void updateModifiedOn(NodeDto record);

    NodeDto.Collection surveyRecords(int surveyId);

    void removeAll(List<Integer> ids, Map<Integer, StatusChange> statusChanges);

    void removeRecord(int recordId);
}
