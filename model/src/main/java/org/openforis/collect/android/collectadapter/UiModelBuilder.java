package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
class UiModelBuilder {
    private final CollectSurvey survey;
    private final Definitions definitions;

    public UiModelBuilder(CollectSurvey survey, Definitions definitions) {
        this.survey = survey;
        this.definitions = definitions;
    }

    public UiSurvey createUiSurvey() {
        UiSurvey uiSurvey = new UiSurvey(survey.getId(), definitions.schemaDefinition());
        for (EntityDefinition recordDefinition : survey.getSchema().getRootEntityDefinitions())
            uiSurvey.addChild(createUiRecordCollection(recordDefinition));
        return uiSurvey;
    }

    private UiRecordCollection createUiRecordCollection(EntityDefinition recordDefinition) {
        return new UiRecordCollection(IdGenerator.nextId(), definitions.toCollectionDefinition(recordDefinition));
    }

    public UiRecord createRecord(CollectRecord record, UiSurvey uiSurvey) {
        return new UiRecordUpdate().create(record, uiSurvey);
    }

    public UiEntity addUiEntity(Entity entity, UiEntityCollection uiEntityCollection) {
        return new UiRecordUpdate().addUiEntity(entity, uiEntityCollection);
    }

    private class UiRecordUpdate {
        UiRecord uiRecord;

        UiRecord create(CollectRecord record, UiSurvey uiSurvey) {
            Entity rootEntity = record.getRootEntity();
            uiRecord = instantiateUiRecord(rootEntity, uiSurvey);
            uiRecord.addChildren(createUiEntityChildrenNodes(rootEntity));
            uiRecord.init();
            return uiRecord;
        }

        UiEntity addUiEntity(Entity entity, UiEntityCollection uiEntityCollection) {
            if (uiRecord == null)
                uiRecord = uiEntityCollection.getUiRecord();
            UiEntity uiEntity = instantiateUiEntity(entity);
            uiEntity.addChildren(createUiEntityChildrenNodes(entity));
            uiEntityCollection.addChild(uiEntity);
            uiEntity.init();
            return uiEntity;
        }

        private UiNode createUiNode(NodeDefinition nodeDefinition, Entity parentEntity) {
            if (nodeDefinition instanceof AttributeDefinition)
                return nodeDefinition.isMultiple()
                        ? createUiAttributeCollection((AttributeDefinition) nodeDefinition, parentEntity)
                        : createUiAttribute((AttributeDefinition) nodeDefinition, parentEntity);
            else if (nodeDefinition instanceof EntityDefinition)
                return nodeDefinition.isMultiple()
                        ? createUiEntityCollection((EntityDefinition) nodeDefinition, parentEntity)
                        : createUiEntity((EntityDefinition) nodeDefinition, parentEntity);
            throw new IllegalStateException("Unsupported node type: " + nodeDefinition);
        }

        private UiNode createUiAttribute(AttributeDefinition attributeDefinition, Entity parentEntity) {
            Attribute attribute = (Attribute) childNode(parentEntity, attributeDefinition);
            UiAttribute uiAttribute = instantiateUiAttribute(attribute);
            if (uiAttribute.isEmpty())
                uiAttribute.setStatus(UiNode.Status.EMPTY);
            else if (!attributeDefinition.getAttributeDefaults().isEmpty())
                uiAttribute.setStatus(UiNode.Status.PENDING_VALIDATION); // An attribute with default value should be validated
            return uiAttribute;
        }

        private UiNode createUiAttributeCollection(AttributeDefinition attributeDefinition, Entity parentEntity) {
            UiAttributeCollection uiAttributeCollection = instantiateUiAttributeCollection(attributeDefinition, parentEntity);
            setRelevance(parentEntity, uiAttributeCollection);
            List<Node<? extends NodeDefinition>> childAttributes = parentEntity.getChildren(attributeDefinition.getName());
            for (Node<? extends NodeDefinition> childAttribute : childAttributes)
                uiAttributeCollection.addChild(instantiateUiAttribute((Attribute) childAttribute));
            return uiAttributeCollection;
        }

        private UiEntity createUiEntity(EntityDefinition entityDefinition, Entity parentEntity) {
            Entity entity = (Entity) childNode(parentEntity, entityDefinition);
            UiEntity uiEntity = instantiateUiEntity(entity);
            uiEntity.addChildren(createUiEntityChildrenNodes(entity));
            return uiEntity;
        }

        private List<UiNode> createUiEntityChildrenNodes(Entity entity) {
            List<UiNode> nodes = new ArrayList<UiNode>();
            for (NodeDefinition childDefinition : childDefinitions(entity))
                nodes.add(createUiNode(childDefinition, entity));
            return nodes;
        }

        private UiEntityCollection createUiEntityCollection(EntityDefinition entityDefinition, Entity parentEntity) {
            UiEntityCollection uiEntityCollection = instantiateUiEntityCollection(entityDefinition, parentEntity);
            setRelevance(parentEntity, uiEntityCollection);
            List<Node<? extends NodeDefinition>> childrenEntities = parentEntity.getChildren(entityDefinition.getName());
            for (Node<? extends NodeDefinition> childrenEntity : childrenEntities)
                addUiEntity((Entity) childrenEntity, uiEntityCollection);
            return uiEntityCollection;
        }

        private List<NodeDefinition> childDefinitions(Entity entity) {
            return nonDeprecated(entity.getDefinition().getChildDefinitions());
        }

        private List<NodeDefinition> nonDeprecated(List<NodeDefinition> nodeDefinitions) {
            List<NodeDefinition> definitions = new ArrayList<NodeDefinition>();
            for (NodeDefinition definition : nodeDefinitions) {
                if (definition.getDeprecatedVersion() == null) // TODO: What if it's not deprecated in current version?
                    definitions.add(definition);
            }
            return definitions;
        }

        private Node childNode(Entity entity, NodeDefinition childDefinition) {
            Node<? extends NodeDefinition> child = entity.getChild(childDefinition.getName());
            if (child == null)
                throw new IllegalStateException("Child not found in entity: " + entity + ". Expected to find " + childDefinition);
            return child;
        }

        private UiEntity instantiateUiEntity(Entity entity) {
            entity.setId(IdGenerator.nextId());
            UiEntity uiEntity = new UiEntity(entity.getId(), isRelevant(entity), definitions.toDefinition(entity));
            setRelevance(entity.getParent(), uiEntity);
            return uiEntity;
        }

        protected boolean isRelevant(Node node) {
            return node.getParent().isRelevant(node.getName());
        }


        private void setRelevance(Entity parentEntity, UiEntity uiEntity) {
            uiEntity.setRelevant(parentEntity.isRelevant(uiEntity.getName()));
        }

        private void setRelevance(Entity parentEntity, UiEntityCollection uiEntityCollection) {
            uiEntityCollection.setRelevant(parentEntity.isRelevant(uiEntityCollection.getName()));
        }

        private void setRelevance(Entity parentEntity, UiAttributeCollection uiAttributeCollection) {
            uiAttributeCollection.setRelevant(parentEntity.isRelevant(uiAttributeCollection.getName()));
        }

        private UiAttribute instantiateUiAttribute(Attribute attribute) {
            attribute.setId(IdGenerator.nextId());
            Definition definition = definitions.toDefinition(attribute);
            return AttributeConverter.toUiAttribute((UiAttributeDefinition) definition, attribute);
        }

        private UiRecord instantiateUiRecord(Entity rootEntity, UiSurvey uiSurvey) {
            rootEntity.setId(IdGenerator.nextId());
            String name = rootEntity.getName();
            UiRecordCollection collection = uiSurvey.lookupRecordCollection(name);
            return new UiRecord(rootEntity.getId(), definitions.toDefinition(rootEntity), collection);
        }

        private UiAttributeCollection instantiateUiAttributeCollection(AttributeDefinition attributeDefinition, Entity parentEntity) {
            return new UiAttributeCollection(
                    IdGenerator.nextId(),
                    parentEntity.getId(),
                    parentEntity.isRelevant(attributeDefinition.getName()),
                    (UiAttributeCollectionDefinition) definitions.toCollectionDefinition(attributeDefinition)
            );
        }

        private UiEntityCollection instantiateUiEntityCollection(EntityDefinition entityDefinition, Entity parentEntity) {
            return new UiEntityCollection(
                    IdGenerator.nextId(),
                    parentEntity.getId(),
                    parentEntity.isRelevant(entityDefinition.getName()),
                    definitions.toCollectionDefinition(entityDefinition)
            );
        }
    }
}
