package org.openforis.collect.android.collectadapter;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.android.NodeEvent;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;
import org.openforis.collect.model.CollectSurvey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.openforis.collect.android.NodeEvent.DELETED;
import static org.openforis.collect.android.NodeEvent.UPDATED;

/**
 * @author Daniel Wiell
 */
public class CollectModelBackedSurveyService implements SurveyService {
    private static final String DATA_EXPORT_DIR = "data_export";
    private static final DateFormat DATA_EXPORT_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.ENGLISH);

    private final ViewModelManager viewModelManager;
    private final CollectModelManager collectModelManager;
    private final File workingDir;

    private SurveyListener listener;
    private boolean updating;
    private List<Runnable> recordUpdateCallbacks = new ArrayList<Runnable>();

    public CollectModelBackedSurveyService(ViewModelManager viewModelManager, CollectModelManager collectModelManager, File workingDir) {
        this.viewModelManager = viewModelManager;
        this.collectModelManager = collectModelManager;
        this.workingDir = workingDir;
    }

    public UiSurvey importSurvey(InputStream inputStream) {
        UiSurvey survey = collectModelManager.importSurvey(inputStream);
        selectSurvey(survey);
        return survey;
    }

    public UiSurvey loadSurvey() {
        UiSurvey survey = collectModelManager.loadSurvey();
        selectSurvey(survey);
        return survey;
    }

    public void selectSurvey(UiSurvey survey) {
        if (survey != null)
            viewModelManager.selectSurvey(survey);
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
        else if (selectedNode instanceof UiEntityCollection)
            lazilyInitValidationErrors((UiEntityCollection) selectedNode);
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

    private void lazilyInitValidationErrors(UiEntityCollection entityCollection) {
        if (entityCollection.getValidationErrors() == null) {
            Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.validateEntityCollection(entityCollection);
            UiNodeChange entityChange = nodeChanges.get(entityCollection);
            if (entityChange != null) {
                entityCollection.setValidationErrors(entityChange.validationErrors);
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
        this.updating = true;
        try {
            UiEntityCollection entityCollection = viewModelManager.selectedEntityCollection();
            NodeAddedResult<UiEntity> result = collectModelManager.addEntity(entityCollection);
            viewModelManager.addEntity(result.nodeAdded, result.nodeChanges);
            updateCalculatedAttributes(result.nodeChanges);
            return result.nodeAdded;
        } finally {
            onRecordUpdateComplete();
        }
    }

    public UiCodeAttribute addCodeAttribute(UiCode code, String qualifier) {  // TODO: Ugly. Do in transaction, redundant updating...
        this.updating = true;
        try {
            // Remove validation errors from the unspecified attribute
            // attribute update will be dispatched later, when newly created attribute will be filled with values
            UiCodeAttribute attribute = (UiCodeAttribute) addAttribute(false);
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
        } finally {
            onRecordUpdateComplete();
        }
    }

    public UiAttribute addAttribute(boolean notifyAttributeUpdate) {
        this.updating = true;
        try {
            UiAttributeCollection attributeCollection = viewModelManager.selectedAttributeCollection();
            NodeAddedResult<UiAttribute> result = collectModelManager.addAttribute(attributeCollection);
            UiAttribute attribute = result.nodeAdded;

            // TODO: Move this section to viewModelManager
            attributeCollection.addChild(attribute);
            attribute.init(); // TODO: Don't want to care about these life-cycle methods here!!!
            attribute.updateStatusOfParents();
            viewModelManager.addAttribute(attribute, result.nodeChanges);
            if (notifyAttributeUpdate) {
                updateAttribute(attribute);
            }
            return attribute;
        } finally {
            onRecordUpdateComplete();
        }
    }

    public UiAttribute addAttribute() {
        return addAttribute(true);
    }

    public void deletedAttribute(int attributeId) {
        this.updating = true;
        try {
            UiNode node = selectedNode().getUiRecord().lookupNode(attributeId);
            if (!(node instanceof UiAttribute))
                throw new IllegalArgumentException("Node with id " + attributeId + " is not an attribute: " + node);
            UiAttribute attribute = (UiAttribute) node;
            Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.removeAttribute(attribute);
            viewModelManager.removeNode(attribute, nodeChanges);
            handleNodeChanges(DELETED, attribute, nodeChanges);
        } finally {
            onRecordUpdateComplete();
        }
    }

    public void deleteEntities(Collection<Integer> entityIds) {
        this.updating = true;
        try {
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
        } finally {
            onRecordUpdateComplete();
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

    @Override
    public void notifyAttributeChanging(UiAttribute attribute) {
        if (listener == null)
            return;
        listener.onNodeChanging(attribute);
    }

    public void updateAttributes(Set<UiAttribute> attributes) {
        if (attributes == null)
            return;
        for (UiAttribute attribute : attributes)
            updateAttribute(attribute);  // TODO: Do this in transaction
    }

    public void updateAttribute(UiAttribute attributeToUpdate) {
        this.updating = true;
        try {
            Map<UiNode, UiNodeChange> nodeChanges = collectModelManager.updateAttribute(attributeToUpdate);
            viewModelManager.updateAttribute(attributeToUpdate, nodeChanges);
            handleNodeChanges(UPDATED, attributeToUpdate, nodeChanges);
        } finally {
            onRecordUpdateComplete();
        }
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

    public File exportSurvey(File surveysDir, boolean excludeBinaries, List<Integer> filterRecordIds) throws IOException {
        // preserve previously selected record (if any)
        Integer oldSelectedRecordId = viewModelManager.getSelectedRecordId();
        File exportedFile = exportFile(surveysDir);
        try {
            collectModelManager.exportSurvey(viewModelManager.getSelectedSurvey(), exportedFile, excludeBinaries, filterRecordIds,
                    new CollectModelManager.ExportListener() {
                        public void beforeRecordExport(int recordId) {
                            selectRecord(recordId);
                        }
                    });
            if (oldSelectedRecordId != null)
                selectRecord(oldSelectedRecordId);
        } catch(IOException e) {
            if (exportedFile != null) {
                exportedFile.delete();
            }
            throw e;
        }
        return exportedFile;
    }

    public boolean hasSurveyGuide() {
        try {
            return collectModelManager.hasSurveyGuide();
        } catch (Exception e) {
            return false;
        }
    }

    public File loadSurveyGuide(File outputDir) throws IOException {
        byte[] content = collectModelManager.loadSurveyGuide();
        if (content == null) {
            return null;
        }
        File file = new File(outputDir, getSelectedSurvey().getName() + "_guide.pdf");
        FileUtils.writeByteArrayToFile(file, content);
        return file;
    }

    private File exportFile(File surveysDir) {
        String fileName = viewModelManager.getSelectedSurvey().getName();
        String username = Settings.user().getUsername();
        if (!username.isEmpty())
            fileName += "_" + username;
        String timestamp = DATA_EXPORT_TIMESTAMP_FORMAT.format(new Date());
        fileName += "_" + timestamp + ".collect-data";
        return new File(getDataExportDirectory(surveysDir), fileName);
    }

    public void setListener(SurveyListener listener) {
        this.listener = listener;
    }

    public File file(UiFileAttribute attribute) {
        return collectModelManager.file(attribute);
    }

    public CollectSurvey getSelectedSurvey() {
        return collectModelManager.getSelectedSurvey();
    }

    public boolean isUpdating() {
        return updating;
    }

    public void registerRecordUpdateCallback(Runnable runnable) {
        recordUpdateCallbacks.add(runnable);
    }

    private void notifyNodeSelected(UiNode previous, UiNode selected) {
        if (listener != null)
            listener.onNodeSelected(previous, selected);
    }

    private void onRecordUpdateComplete() {
        updating = false;
        Iterator<Runnable> callbacksIt = recordUpdateCallbacks.iterator();
        while (callbacksIt.hasNext()) {
            Runnable callback = callbacksIt.next();
            try {
                callback.run();
            } catch(Exception e) {
                //do nothing
            }
            callbacksIt.remove();
        }
    }

    private File getDataExportDirectory(File surveysDir) {
        File surveyDir = new File(surveysDir, getSelectedSurvey().getName());
        return new File(surveyDir, DATA_EXPORT_DIR);
    }

}
