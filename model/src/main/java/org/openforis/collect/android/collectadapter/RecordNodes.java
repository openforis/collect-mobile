package org.openforis.collect.android.collectadapter;

import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.openforis.collect.android.collectadapter.CalculatedAttributeUtils.isCalculated;

/**
 * @author Daniel Wiell
 */
class RecordNodes {
    private final Map<Integer, Node> nodeById = new HashMap<Integer, Node>();

    public RecordNodes(CollectRecord record) {
        if (record == null)
            throw new IllegalArgumentException("Record is null");
        add(record.getRootEntity());
    }

    public void add(Entity entity) {
        if (entity == null)
            throw new IllegalArgumentException("Entity is null");
        put(entity);
        for (Node child : entity.getChildren()) {
            add(child);
        }
    }

    public void add(Node node) {
        if (node instanceof Entity)
            add((Entity) node);
        else if (!isCalculated(node)) // TODO: Should we actually ignore calculated attributes?
            put(node);
    }

    public void remove(int nodeId) {
        nodeById.remove(nodeId);
    }

    private void put(Node node) {
        if (node.getId() == null)
            throw new IllegalStateException("Node id is null: " + node);
        nodeById.put(node.getId(), node);
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> T getById(int nodeId, Class<T> type) {
        Node node = nodeById.get(nodeId);
        if (node == null)
            throw new IllegalStateException("Node with id " + nodeId + " not found");

        if (type.isAssignableFrom(((Object) node).getClass()))
            return (T) node;
        throw new IllegalStateException("Expected node with id " + nodeId + " to be of type " + type);
    }

    public Entity getEntityById(int nodeId) {
        return getById(nodeId, Entity.class);
    }

    public CodeAttribute getCodeAttribute(int nodeId) {
        return getById(nodeId, CodeAttribute.class);
    }

    public Attribute getAttribute(int nodeId) {
        return getById(nodeId, Attribute.class);
    }
}
