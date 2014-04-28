package org.openforis.collect.android.viewmodelmanager;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface NodeRepository {

    void insert(List<NodeDto> nodes);

    NodeDto.Collection recordNodes(int recordId);

    void update(NodeDto node, String recordStatus);

    NodeDto.Collection surveyRecords(int surveyId);

    void removeAll(List<Integer> ids);
}
