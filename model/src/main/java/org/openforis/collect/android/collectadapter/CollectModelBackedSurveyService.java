package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.ViewModelManager;

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
        if (selectedNode instanceof UiAttribute)
            lazilyInitValidationErrors((UiAttribute) selectedNode);
        return selectedNode;
    }

    private void lazilyInitValidationErrors(UiAttribute attribute) {
        if (attribute.getValidationErrors() == null) {
            Map<UiAttribute, UiAttributeChange> attributeChanges = attribute.getStatus().isWorseThen(UiNode.Status.EMPTY)
                    ? collectModelManager.updateAttribute(attribute) // TODO: Not semantically an update.
                    : Collections.<UiAttribute, UiAttributeChange>emptyMap();
            UiAttributeChange attributeChange = attributeChanges.get(attribute);
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

    public UiCodeAttribute addCodeAttribute(UiCode code) {  // TODO: Ugly. Do in transaction, redundant updating...
        UiCodeAttribute attribute = (UiCodeAttribute) addAttribute();
        attribute.setCode(code);
        updateAttribute(attribute);
        return attribute;
    }

    public UiAttribute addAttribute() {
        UiAttributeCollection attributeCollection = viewModelManager.selectedAttributeCollection();
        UiAttribute attribute = collectModelManager.addAttribute(attributeCollection);

        // TODO: Move this section to viewModelManager
        attributeCollection.addChild(attribute);
        attribute.init(); // TODO: Ugly!!!
        attribute.updateStatusOfParents(); // TODO: Ugly!!!!
        viewModelManager.addAttribute(attribute);
        updateAttribute(attribute);
        return attribute;
    }

    public void removeAttribute(UiAttribute attribute) {
        collectModelManager.removeAttribute(attribute);
        viewModelManager.removeNode(attribute);
        // TODO: Implement...
    }

    public void updateAttributes(Set<UiAttribute> attributes) {
        if (attributes == null)
            return;
        for (UiAttribute attribute : attributes) {
            updateAttribute(attribute);  // TODO: Do this in transaction?
        }
    }

    public void updateAttribute(UiAttribute attributeToUpdate) {
        Map<UiAttribute, UiAttributeChange> attributeChanges = collectModelManager.updateAttribute(attributeToUpdate);
        // TODO: Do this in transaction
        viewModelManager.updateAttribute(attributeToUpdate, attributeChanges);
        if (listener != null)
            for (UiAttribute uiAttribute : attributeChanges.keySet())
                listener.onAttributeChanged(uiAttribute, attributeChanges);
    }

    public void setListener(SurveyListener listener) {
        this.listener = listener;
    }


    private void notifyNodeSelected(UiNode previous, UiNode selected) {
        if (listener != null)
            listener.onNodeSelected(previous, selected);
    }
}
