package org.openforis.collect.android.viewmodelmanager;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.viewmodel.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class ViewModelManager {
    private final ViewModelRepository repo;
    private UiSurvey selectedSurvey;
    private UiRecord selectedRecord;
    private UiNode selectedNode;

    public ViewModelManager(ViewModelRepository repo) {
        this.repo = repo;
    }

    public UiSurvey getSelectedSurvey() {
        return selectedSurvey;
    }

    public Integer getSelectedRecordId() {
        return selectedRecord == null ? null : selectedRecord.getId();
    }

    public boolean isRecordSelected(int recordId) {
        return selectedRecord != null && selectedRecord.getId() == recordId;
    }

    public void selectSurvey(UiSurvey survey) {
        selectedSurvey = survey;
        selectedRecord = null;
        addRecordPlaceholders(survey);
        selectNode(survey.getFirstChild().getId());
    }

    public void addRecord(UiRecord record) {
        validateRequiredness(record);
        record.updateStatusOfNodeAndDescendants(); // TODO: This should sbe done at record.init()? Ugly anyway
        record.updateStatusOfParents();
        selectedSurvey.addRecord(record);
        repo.insertRecord(record);
        selectedRecord = record;
    }

    public UiRecord selectRecord(int recordId) {
        this.selectedRecord = loadRecord(recordId);
        return this.selectedRecord;
    }

    private UiRecord loadRecord(int recordId) {
        UiRecord record = repo.recordById(selectedSurvey, recordId);
        if (record == null)
            throw new IllegalStateException("No record found with id " + recordId);
        record.updateStatusOfNodeAndDescendants();
        record.updateStatusOfParents(); // TODO: This should be done at record.init()? Ugly anyway
        return record;
    }

    public void addEntity(final UiEntity entity, final Map<UiNode, UiNodeChange> nodeChanges) {
        validateRequiredness(entity);
        entity.updateStatusOfNodeAndDescendants(); // TODO: This should sbe done at record.init()? Ugly anyway
        entity.updateStatusOfParents();
        Timer.time(ViewModelRepository.class, "insertEntity", new Runnable() {
            public void run() {
                repo.insertEntity(entity, statusChanges(nodeChanges));
            }
        });
        updateRecordModifiedDate(entity.getUiRecord());
    }

    private void validateRequiredness(UiInternalNode node) {
        for (UiNode child : node.getChildren()) {
            if (child instanceof UiInternalNode)
                validateRequiredness((UiInternalNode) child);
            if (child instanceof UiAttribute) {
                validateRequiredness((UiAttribute) child);
            }
        }
    }

    private void validateRequiredness(UiAttribute attribute) {
        if (attribute.getDefinition().required && attribute.isEmpty())
            attribute.setStatus(UiNode.Status.VALIDATION_ERROR);
    }

    public UiNode lookupNode(int nodeId) {
        UiNode node = selectedSurvey.lookupRecordCollection(nodeId);
        if (node == null)
            node = selectedRecord.lookupNode(nodeId);
        if (node == null)
            throw new IllegalStateException("Node not found: " + nodeId);
        return node;
    }

    public UiAttributeCollection selectedAttributeCollection() {
        if (!(selectedNode instanceof UiAttributeCollection))
            throw new IllegalStateException("Selected node is not an attribute collection: " + selectedNode);
        return (UiAttributeCollection) selectedNode;
    }

    public UiEntityCollection selectedEntityCollection() {
        if (!(selectedNode instanceof UiEntityCollection))
            throw new IllegalStateException("Selected node is not an entity collection: " + selectedNode);
        return (UiEntityCollection) selectedNode;
    }

    public UiNode selectedNode() {
        return selectedNode;
    }

    public UiNode selectNode(int nodeId) {
        selectedNode = lookupNode(nodeId);
        if (selectedNode instanceof UiRecordCollection)
            selectedRecord = null;
        return selectedNode;
    }

    public void addAttribute(UiAttribute attribute, Map<UiNode, UiNodeChange> nodeChanges) {
        updateRecordModifiedDate(attribute.getUiRecord());
        repo.insertAttribute(attribute, statusChanges(nodeChanges));
    }

    public void updateAttribute(UiAttribute attribute, Map<UiNode, UiNodeChange> nodeChanges) {
        List<UiAttribute> attributesChanged = new ArrayList<UiAttribute>();
        attributesChanged.add(attribute);
        Set<Map.Entry<UiNode, UiNodeChange>> entries = nodeChanges.entrySet();
        for (Map.Entry<UiNode, UiNodeChange> entry : entries) {
            if (entry.getValue().valueChange) {
                attributesChanged.add((UiAttribute) entry.getKey());
            }
        }
        Map<Integer, StatusChange> statusChanges = statusChanges(nodeChanges);
        for (UiAttribute attributeChanged : attributesChanged) {
            attributeChanged.setModifiedOn(new Date());
            repo.updateAttribute(attributeChanged, statusChanges);
        }

        UiRecord record = updateRecordModifiedDate(attribute.getUiRecord());

        if (record.isKeyAttribute(attribute))
            record.keyAttributeUpdated();
    }

    private Map<Integer, StatusChange> statusChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        Map<Integer, StatusChange> statusChanges = new HashMap<Integer, StatusChange>();
        for (Map.Entry<UiNode, UiNodeChange> nodeChangeEntry : nodeChanges.entrySet()) {
            UiNode changedNode = nodeChangeEntry.getKey();
            UiNodeChange nodeChange = nodeChangeEntry.getValue();
            changedNode.setValidationErrors(nodeChange.validationErrors);
            if (nodeChange.relevanceChange || nodeChange.statusChange) {
                if (nodeChange.relevanceChange)
                    changedNode.setRelevant(!changedNode.isRelevant());
                changedNode.updateStatus(nodeChange.validationErrors);
                statusChanges.put(changedNode.getId(), new StatusChange(changedNode));
            }
        }

        for (Map.Entry<UiNode, UiNodeChange> nodeChangeEntry : nodeChanges.entrySet()) {
            UiNode changedNode = nodeChangeEntry.getKey();
            List<UiNode> updatedParents = changedNode.updateStatusOfParents();
            for (UiNode updatedParent : updatedParents) {
                statusChanges.put(updatedParent.getId(), new StatusChange(updatedParent));
            }
        }
        return statusChanges;
    }

    private void addRecordPlaceholders(UiSurvey uiSurvey) {
        List<UiRecord.Placeholder> recordPlaceholders = repo.surveyRecords(uiSurvey.getId());
        for (UiRecord.Placeholder record : recordPlaceholders)
            uiSurvey.lookupRecordCollection(record.getRecordCollectionName()).addChild(record);
    }

    public void removeNode(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        Map<Integer, StatusChange> statusChanges = statusChanges(nodeChanges);
        node.getParent().setModifiedOn(new Date());
        node.removeFromParent();
        List<UiNode> updatedParents = node.updateStatusOfParents();
        for (UiNode updatedParent : updatedParents)
            statusChanges.put(updatedParent.getId(), new StatusChange(updatedParent));
        repo.removeNode(node, statusChanges);

        updateRecordModifiedDate(node.getUiRecord());
    }

    public void removeRecord(UiRecord.Placeholder record) {
        record.removeFromParent();
        repo.removeRecord(record.getId());
    }

    private UiRecord updateRecordModifiedDate(UiRecord record) {
        record.setModifiedOn(new Date());
        repo.updateRecordModifiedOn(record);
        record.modifiedOnUpdated();
        return record;
    }

}
