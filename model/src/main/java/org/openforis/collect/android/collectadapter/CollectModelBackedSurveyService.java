package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class CollectModelBackedSurveyService implements SurveyService {
    private final ViewModelManager viewModelManager;
    private final CollectModelManager collectModelManager;
    private final File exportFile;

    private SurveyListener listener;

    public CollectModelBackedSurveyService(ViewModelManager viewModelManager, CollectModelManager collectModelManager, File exportFile) {
        this.viewModelManager = viewModelManager;
        this.collectModelManager = collectModelManager;
        this.exportFile = exportFile;
    }

    public UiSurvey importSurvey(InputStream inputStream) {
        UiSurvey survey = collectModelManager.importSurvey(inputStream);
        viewModelManager.selectSurvey(survey);
        return survey;
    }

    public UiSurvey loadSurvey() {
        UiSurvey survey = collectModelManager.loadSurvey();
        if (survey != null)
            viewModelManager.selectSurvey(survey);
        return survey;

    }

    public UiRecord addRecord(String entityName) {
        UiRecord record = collectModelManager.addRecord(entityName, viewModelManager.getSelectedSurvey());
        viewModelManager.addRecord(record);
        return record;
    }

    public boolean isRecordSelected(int recordId) {
        return viewModelManager.isRecordSelected(recordId);
    }

    public UiRecord selectRecord(int recordId) {
        UiRecord record = viewModelManager.selectRecord(recordId);
        collectModelManager.recordSelected(record);
        return record;
    }

    public UiNode selectNode(int nodeId) {
        UiNode previousNode = selectedNode();
        if (previousNode != null && nodeId == previousNode.getId())
            return previousNode; // Do nothing if already selected
        UiNode selectedNode = viewModelManager.selectNode(nodeId);
        notifyNodeSelected(previousNode, selectedNode);
        if (selectedNode instanceof UiAttribute)
            lazilyInitValidationErrors((UiAttribute) selectedNode);
        return selectedNode;
    }

    private void lazilyInitValidationErrors(UiAttribute attribute) {
        if (attribute.getValidationErrors() == null) {
            Map<UiNode, UiNodeChange> nodeChanges = attribute.getStatus().isWorseThen(UiNode.Status.EMPTY)
                    ? collectModelManager.updateAttribute(attribute) // TODO: Not semantically an update.
                    : Collections.<UiNode, UiNodeChange>emptyMap();
            UiNodeChange attributeChange = nodeChanges.get(attribute);
            if (attributeChange != null)
                attribute.setValidationErrors(attributeChange.validationErrors);
        }
    }

    public UiNode selectedNode() {
        return viewModelManager.selectedNode();
    }

    public UiNode lookupNode(int nodeId) {
        return viewModelManager.lookupNode(nodeId);
    }

    public UiEntity addEntity() {
        UiEntityCollection entityCollection = viewModelManager.selectedEntityCollection();
        UiEntity entity = collectModelManager.addEntity(entityCollection);
        viewModelManager.addEntity(entity);
        return entity;
    }

    public UiCodeAttribute addCodeAttribute(UiCode code, String qualifier) {  // TODO: Ugly. Do in transaction, redundant updating...
        UiCodeAttribute attribute = (UiCodeAttribute) addAttribute();
        attribute.setCode(code);
        attribute.setQualifier(qualifier);
        updateAttribute(attribute);
        return attribute;
    }

    public UiAttribute addAttribute() {
        UiAttributeCollection attributeCollection = viewModelManager.selectedAttributeCollection();
        UiAttribute attribute = collectModelManager.addAttribute(attributeCollection);

        // TODO: Move this section to viewModelManager
        attributeCollection.addChild(attribute);
        attribute.init(); // TODO: Don't want to care about these life-cycle methods here!!!
        attribute.updateStatusOfParents();
        viewModelManager.addAttribute(attribute);
        updateAttribute(attribute);
        return attribute;
    }

    public void removeAttribute(int attributeId) {
        UiNode node = selectedNode().getUiRecord().lookupNode(attributeId);
        if (!(node instanceof UiAttribute))
            throw new IllegalArgumentException("Node with id " + attributeId + " is not an attribute: " + node);
        UiAttribute attribute = (UiAttribute) node;
        collectModelManager.removeAttribute(attribute);
        viewModelManager.removeNode(attribute);
    }

    public void removeEntity(int entityId) {
        UiNode node = selectedNode().getUiRecord().lookupNode(entityId);
        if (!(node instanceof UiEntity))
            throw new IllegalArgumentException("Node with id " + entityId + " is not an entity: " + node);
        UiEntity entity = (UiEntity) node;
        collectModelManager.removeEntity(entity.getId());
        viewModelManager.removeNode(entity);
    }

    public void removeRecord(int recordId) {
        UiRecordCollection recordCollection = (UiRecordCollection) selectedNode();
        UiRecord.Placeholder record = (UiRecord.Placeholder) recordCollection.getChildById(recordId);
        viewModelManager.removeNode(record);
    }

    public void updateAttributes(Set<UiAttribute> attributes) {
        if (attributes == null)
            return;
        for (UiAttribute attribute : attributes)
            updateAttribute(attribute);  // TODO: Do this in transaction
    }

    public void updateAttribute(UiAttribute attributeToUpdate) {
        Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.updateAttribute(attributeToUpdate);
        viewModelManager.updateAttribute(attributeToUpdate, nodeChanges);
        if (listener == null)
            return;
        listener.onNodeChanged(attributeToUpdate, nodeChanges);
        updateCalculatedAttributes(nodeChanges);
    }

    private void updateCalculatedAttributes(Map<UiNode, UiNodeChange> nodeChanges) {
        for (UiNode uiNode : nodeChanges.keySet())
            if (uiNode instanceof UiAttribute && ((UiAttribute) uiNode).isCalculated()) {
                // TODO: Do this in same transaction as value update, but ideally don't persist at all
                viewModelManager.updateAttribute((UiAttribute) uiNode, nodeChanges);
                listener.onNodeChanged(uiNode, nodeChanges);
            }
    }

    public void exportSurvey() throws IOException {
        Integer selectedRecordId = viewModelManager.getSelectedRecordId();

        collectModelManager.exportSurvey(viewModelManager.getSelectedSurvey(), exportFile, new CollectModelManager.ExportListener() {
            public void beforeRecordExport(int recordId) {
                selectRecord(recordId);
            }
        });

        if (selectedRecordId != null)
            selectRecord(selectedRecordId);
    }

    public void setListener(SurveyListener listener) {
        this.listener = listener;
    }


    private void notifyNodeSelected(UiNode previous, UiNode selected) {
        if (listener != null)
            listener.onNodeSelected(previous, selected);
    }
}
