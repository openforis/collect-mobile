package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;

import java.io.InputStream;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class CollectModelBackedSurveyService implements SurveyService {
    private final ViewModelManager viewModelManager;
    private final CollectModelManager collectModelManager;

    private SurveyListener listener;

    public CollectModelBackedSurveyService(ViewModelManager viewModelManager, CollectModelManager collectModelManager) {
        this.viewModelManager = viewModelManager;
        this.collectModelManager = collectModelManager;
    }

    public UiSurvey importSurvey(InputStream inputStream) {
        UiSurvey survey = collectModelManager.importSurvey(inputStream);
        viewModelManager.selectSurvey(survey);
        return survey;
    }

    public UiSurvey loadSurvey(String name) {
        UiSurvey survey = collectModelManager.loadSurvey(name);
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
        return selectedNode;
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

    public UiAttribute addAttribute() {
        UiAttributeCollection attributeCollection = viewModelManager.selectedAttributeCollection();
        UiAttribute attribute = collectModelManager.addAttribute(attributeCollection);
        // TODO: Create the attribute elsewhere, save it, etc...
        attributeCollection.addChild(attribute);
        updateAttribute(attribute);
        return attribute;
    }

    public void updateAttributeCollection(UiAttributeCollection attributeCollection) {
        for (UiNode attribute : attributeCollection.getChildren())
            updateAttribute((UiAttribute) attribute); // TODO: Do this in transaction?
        // Push to viewModelManager? Separate notification (listener.onAttributeChanged)?
    }

    public void updateAttribute(UiAttribute attribute) {
        Set<UiValidationError> validationErrors = collectModelManager.updateAttribute(attribute);
        viewModelManager.updateAttribute(attribute, validationErrors);
        if (listener != null)
            listener.onAttributeChanged(attribute, validationErrors);
    }

    public void setListener(SurveyListener listener) {
        this.listener = listener;
    }


    private void notifyNodeSelected(UiNode previous, UiNode selected) {
        if (listener != null)
            listener.onNodeSelected(previous, selected);
    }
}
