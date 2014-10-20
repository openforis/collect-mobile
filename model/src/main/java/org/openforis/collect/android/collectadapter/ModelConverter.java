package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
class ModelConverter {
    private final CollectSurvey collectSurvey;
    private final Definitions definitions;

    ModelConverter(CollectSurvey collectSurvey, Definitions definitions) {
        this.collectSurvey = collectSurvey;
        this.definitions = definitions;
    }

    public UiSurvey toUiSurvey() {
        return new UiModelBuilder(collectSurvey, definitions).createUiSurvey();
    }

    public UiRecord toUiRecord(CollectRecord collectRecord, UiSurvey uiSurvey) {
        return new UiModelBuilder(collectSurvey, definitions).createRecord(collectRecord, uiSurvey);
    }

    public UiEntity toUiEntity(CollectSurvey collectSurvey, Entity entity, UiEntityCollection uiEntityCollection) {
        return new UiModelBuilder(collectSurvey, definitions).addUiEntity(entity, uiEntityCollection);
    }

    public UiCodeList toUiCodeList(List<CodeListItem> codeList) {
        List<UiCode> uiCodes = new ArrayList<UiCode>();
        UiCode qualifiableCode = null;
        for (CodeListItem item : codeList) {
            UiCode code = new UiCode(item.getCode(), item.getLabel());
            if (item.isQualifiable())
                qualifiableCode = code;
            uiCodes.add(code);
        }
        return new UiCodeList(uiCodes, qualifiableCode);
    }

    public CollectRecord toCollectRecord(UiRecord uiRecord, CollectSurvey collectSurvey) {
        CollectRecord collectRecord = new CollectRecord(collectSurvey, lastVersion(collectSurvey));
        collectRecord.setId(uiRecord.getId());
        Entity rootEntity = collectRecord.createRootEntity(uiRecord.getName());
        rootEntity.setId(uiRecord.getId());
        addChildNodes(rootEntity, uiRecord, collectRecord);
        return collectRecord;
    }

    private String lastVersion(CollectSurvey collectSurvey) {
        List<ModelVersion> versions = collectSurvey.getVersions();
        return versions.isEmpty() ? null : versions.get(versions.size() - 1).getName();
    }

    private void addChildNodes(Entity entity, UiInternalNode uiNode, CollectRecord collectRecord) {
        for (UiNode childUiNode : uiNode.getChildren()) {
            if (childUiNode instanceof UiEntity)
                entity.add(toEntity((UiEntity) childUiNode, collectRecord));
            else if (childUiNode instanceof UiAttribute)
                entity.add(toAttribute((UiAttribute) childUiNode));
            else if (childUiNode instanceof UiInternalNode)
                addChildNodes(entity, (UiInternalNode) childUiNode, collectRecord);
        }
    }

    private Node toAttribute(UiAttribute uiAttribute) {
        return AttributeConverter.toAttribute(uiAttribute, attributeDefinition(uiAttribute));
    }

    private NodeDefinition attributeDefinition(UiAttribute uiAttribute) {
        return definitions.toNodeDefinition(uiAttribute.getDefinition());
    }

    private Entity toEntity(UiEntity uiEntity, CollectRecord collectRecord) {
        Entity entity = new Entity(entityDefinition(uiEntity));
        entity.setId(uiEntity.getId());
        addChildNodes(entity, uiEntity, collectRecord);
        return entity;
    }

    private EntityDefinition entityDefinition(UiEntity entity) {
        return (EntityDefinition) definitions.toNodeDefinition(entity.getDefinition());
    }

}
