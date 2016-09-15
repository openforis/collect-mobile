package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.NodeEvent;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openforis.collect.android.NodeEvent.DELETED;
import static org.openforis.collect.android.NodeEvent.UPDATED;

/**
 * @author Daniel Wiell
 */
public class CollectModelBackedSurveyService implements SurveyService {
    private final ViewModelManager viewModelManager;
    private final CollectModelManager collectModelManager;
    private final File workingDir;

    private SurveyListener listener;

    public CollectModelBackedSurveyService(ViewModelManager viewModelManager, CollectModelManager collectModelManager, File workingDir) {
        this.viewModelManager = viewModelManager;
        this.collectModelManager = collectModelManager;
        this.workingDir = workingDir;
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
        collectModelManager.recordSelected(record);
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
                    ? collectModelManager.validateAttribute(attribute)
                    : Collections.<UiNode, UiNodeChange>emptyMap();
            UiNodeChange attributeChange = nodeChanges.get(attribute);
            if (attributeChange != null) {
                attribute.setValidationErrors(attributeChange.validationErrors);
                if (attributeChange.statusChange)
                    updateAttribute(attribute);
            }
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
        NodeAddedResult<UiEntity> result = collectModelManager.addEntity(entityCollection);
        UiEntity entity = result.nodeAdded;
        viewModelManager.addEntity(entity);
        updateCalculatedAttributes(result.nodeChanges);
        return entity;
    }

    public UiCodeAttribute addCodeAttribute(UiCode code, String qualifier) {  // TODO: Ugly. Do in transaction, redundant updating...
        // Remove validation errors from the unspecified attribute

        UiCodeAttribute attribute = (UiCodeAttribute) addAttribute();
        attribute.setCode(code);
        attribute.setQualifier(qualifier);

        UiAttributeCollection attributeCollection = viewModelManager.selectedAttributeCollection();
        Map<UiNode, UiNodeChange> resetErrorChanges = new HashMap<UiNode, UiNodeChange>();
        for (UiNode sibling : attributeCollection.getChildren())
            if (attribute != sibling
                    && ((UiCodeAttribute) sibling).getCode() == null
                    && sibling.getStatus() != UiNode.Status.OK) {
                sibling.setValidationErrors(Collections.<UiValidationError>emptySet());
                sibling.setStatus(UiNode.Status.OK);
                resetErrorChanges.put(sibling, UiNodeChange.statusChanged());
            }

        Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.updateAttribute(attribute);
        nodeChanges.putAll(resetErrorChanges);
        viewModelManager.updateAttribute(attribute, nodeChanges);
        handleNodeChanges(UPDATED, attribute, nodeChanges);
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

    public void deletedAttribute(int attributeId) {
        UiNode node = selectedNode().getUiRecord().lookupNode(attributeId);
        if (!(node instanceof UiAttribute))
            throw new IllegalArgumentException("Node with id " + attributeId + " is not an attribute: " + node);
        UiAttribute attribute = (UiAttribute) node;
        Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.removeAttribute(attribute);
        viewModelManager.removeNode(attribute, nodeChanges);
        handleNodeChanges(DELETED, attribute, nodeChanges);
    }

    public void deleteEntities(Collection<Integer> entityIds) {
        // TODO: Do in transaction
        for (Integer entityId : entityIds) {
            UiNode node = selectedNode().getUiRecord().lookupNode(entityId);
            if (!(node instanceof UiEntity))
                throw new IllegalArgumentException("Node with id " + entityId + " is not an entity: " + node);
            UiEntity entity = (UiEntity) node;
            Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.removeEntity(entity);
            viewModelManager.removeNode(entity, nodeChanges);
            handleNodeChanges(DELETED, entity, nodeChanges);
        }
    }

    public void deleteRecords(Collection<Integer> recordIds) {
        // TODO: Do in transaction
        for (Integer recordId : recordIds) {
            UiRecordCollection recordCollection = (UiRecordCollection) selectedNode();
            UiRecord.Placeholder record = (UiRecord.Placeholder) recordCollection.getChildById(recordId);
            viewModelManager.removeRecord(record);
        }
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
        handleNodeChanges(UPDATED, attributeToUpdate, nodeChanges);
    }

    private void handleNodeChanges(NodeEvent event, UiNode updatedNode, Map<UiNode, UiNodeChange> nodeChanges) {
        if (listener == null)
            return;
        listener.onNodeChanged(event, updatedNode, nodeChanges);
        updateCalculatedAttributes(nodeChanges);
    }

    private void updateCalculatedAttributes(Map<UiNode, UiNodeChange> nodeChanges) {
        Map<UiNode, UiNodeChange> emptyMap = Collections.emptyMap();
        for (UiNode uiNode : nodeChanges.keySet())
            if (uiNode instanceof UiAttribute && uiNode.isCalculated()) {
                // TODO: Do this in same transaction as value update, but ideally don't persist at all
                viewModelManager.updateAttribute((UiAttribute) uiNode, emptyMap);
                listener.onNodeChanged(UPDATED, uiNode, emptyMap);
            }
    }

    public File exportSurvey() throws IOException {
        Integer selectedRecordId = viewModelManager.getSelectedRecordId();

        File exportedFile = exportFile();
        collectModelManager.exportSurvey(viewModelManager.getSelectedSurvey(), exportedFile, new CollectModelManager.ExportListener() {
            public void beforeRecordExport(int recordId) {
                selectRecord(recordId);
            }
        });

        if (selectedRecordId != null)
            selectRecord(selectedRecordId);

        return exportedFile;
    }

    private File exportFile() {
        String fileName = viewModelManager.getSelectedSurvey().getName();
        String username = Settings.user().getName();
        if (!username.isEmpty())
            fileName += "_" + username;
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm").format(new Date());
        fileName += "_" + timestamp + ".collect-data";
        return new File(workingDir, fileName);
    }

    public void setListener(SurveyListener listener) {
        this.listener = listener;
    }

    public File file(UiFileAttribute attribute) {
        return collectModelManager.file(attribute);
    }

    private void notifyNodeSelected(UiNode previous, UiNode selected) {
        if (listener != null)
            listener.onNodeSelected(previous, selected);
    }
}
