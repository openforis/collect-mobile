package org.openforis.collect.android.viewmodelmanager;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.viewmodel.*;

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

    public boolean isRecordSelected(int recordId) {
        return selectedNode != null && selectedRecord.getId() == recordId;
    }

    public void selectSurvey(UiSurvey survey) {
        selectedSurvey = survey;
        addRecordPlaceholders(survey);
        selectNode(survey.getFirstChild().getId());
    }

    public void addRecord(UiRecord record) {
        record.updateStatusOfNodeAndDescendants(); // TODO: This should sbe done at record.init()? Ugly anyway
        record.updateStatusOfParents();
        selectedSurvey.addRecord(record);
        repo.insertRecord(record);
        selectedRecord = record;
    }

    public UiRecord selectRecord(int recordId) {
        selectedRecord = repo.recordById(selectedSurvey, recordId);
        selectedRecord.updateStatusOfNodeAndDescendants();
        selectedRecord.updateStatusOfParents(); // TODO: This should sbe done at record.init()? Ugly anyway
        if (selectedRecord == null)
            throw new IllegalStateException("No record found with id " + recordId);
        return selectedRecord;
    }

    public void addEntity(final UiEntity entity) {
        entity.updateStatusOfNodeAndDescendants(); // TODO: This should sbe done at record.init()? Ugly anyway
        entity.updateStatusOfParents();
        Timer.time(ViewModelRepository.class, "insertEntity", new Runnable() {
            public void run() {
                repo.insertEntity(entity);
            }
        });
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
        return selectedNode;
    }

    public void addAttribute(UiAttribute attribute) {
        repo.insertAttribute(attribute);
    }

    public void updateAttribute(UiAttribute attribute, Map<UiAttribute, Set<UiValidationError>> validationErrors) {
        UiNode.Status oldRecordStatus = attribute.getUiRecord().getStatus();
        for (UiAttribute attributeWithValidationError : validationErrors.keySet()) {
            Set<UiValidationError> errors = validationErrors.get(attributeWithValidationError);
            attributeWithValidationError.setValidationErrors(errors);
            attributeWithValidationError.updateStatus(errors);
        }
        UiNode.Status newRecordStatus = attribute.getUiRecord().getStatus();
        if (oldRecordStatus == newRecordStatus)
            repo.updateAttribute(attribute);
        else
            repo.updateAttribute(attribute, newRecordStatus);
    }

    private void addRecordPlaceholders(UiSurvey uiSurvey) {
        List<UiRecord.Placeholder> recordPlaceholders = repo.surveyRecords(uiSurvey.getId());
        for (UiRecord.Placeholder record : recordPlaceholders)
            uiSurvey.lookupRecordCollection(record.getRecordCollectionName()).addChild(record);
    }

    public void removeNode(UiNode node) {
        repo.removeNode(node);
    }
}
