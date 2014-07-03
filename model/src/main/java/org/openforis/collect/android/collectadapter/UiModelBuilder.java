package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Clean up

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
            uiRecord.addChildren(createUiNodesForTabs(rootTabs(rootEntity), rootEntity));
            uiRecord.init();
            return uiRecord;
        }

        UiEntity addUiEntity(Entity entity, UiEntityCollection uiEntityCollection) {
            if (uiRecord == null)
                uiRecord = uiEntityCollection.getUiRecord();
            UiEntity uiEntity = instantiateUiEntity(entity);
            uiEntity.addChildren(createUiEntityChildrenNodes(entity));
            uiEntity.addChildren(createUiNodesForEntityChildrenTabs(entity));
            uiEntityCollection.addChild(uiEntity);
            uiEntity.init();
            return uiEntity;
        }

        private List<UiNode> createUiNodesForEntityChildrenTabs(Entity parentEntity) {
            UITab entityTab = tab(parentEntity);
            if (entityTab == null)
                return Collections.emptyList();
            return createUiNodesForTabs(entityTab.getTabs(), parentEntity);
        }

        private List<UiNode> createUiNodesForTabs(List<UITab> tabs, Entity parentEntity) {
            List<UiNode> nodes = new ArrayList<UiNode>();
            for (UITab tab : tabs) {
                UiNode tabNode = createUiNodeForTab(tab, parentEntity);
                nodes.add(tabNode);
            }
            return nodes;
        }

        private UiNode createUiNodeForTab(UITab tab, Entity parentEntity) {
            List<UiNode> nodes = new ArrayList<UiNode>();
            nodes.addAll(createUiNodesForTabAssociations(tab, parentEntity));
            nodes.addAll(createUiNodesForTabChildrenTabs(tab, parentEntity));
            boolean singleUiInternalNode = nodes.size() == 1 && nodes.get(0) instanceof UiInternalNode;
            if (singleUiInternalNode) {
//                UiNode node = nodes.get(0);
//                node.setLabel(label(tab)); // TODO: Is this what we want?
                return nodes.get(0); // Skip intermediate node
            }
            UiInternalNode node = instantiateTabUiNode(tab);
            node.addChildren(nodes);
            return node;
        }

        private List<UiNode> createUiNodesForTabAssociations(UITab tab, Entity parentEntity) {
            List<UiNode> nodes = new ArrayList<UiNode>();
            for (NodeDefinition nodeDefinition : tabAssociations(tab))
                if (!(nodeDefinition instanceof CalculatedAttributeDefinition))
                    nodes.add(createUiNode(nodeDefinition, parentEntity));
            return nodes;
        }

        private List<UiNode> createUiNodesForTabChildrenTabs(UITab tab, Entity parentEntity) {
            List<UiNode> childTabNodes = new ArrayList<UiNode>();
            for (UITab childTab : tab.getTabs()) {
                EntityDefinition entityDefinition = entityDefinition(childTab);
                if (entityDefinition != null && !entityDefinition.isMultiple())
                    childTabNodes.add(createUiNodeForTab(childTab, (Entity) childNode(parentEntity, entityDefinition)));
            }
            return childTabNodes;
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
            return uiAttribute;
        }

        private UiNode createUiAttributeCollection(AttributeDefinition attributeDefinition, Entity parentEntity) {
            UiAttributeCollection uiAttributeCollection = instantiateUiAttributeCollection(attributeDefinition, parentEntity);
            setRelevance(parentEntity, uiAttributeCollection);
            List<Node<? extends NodeDefinition>> childAttributes = parentEntity.getAll(attributeDefinition.getName());
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
                if (isAssignedToDifferentTabs(childDefinition, entity)) // TODO: Should we actually ignore calculated attributes?
                    nodes.add(createUiNode(childDefinition, entity));
            return nodes;
        }

        private boolean isAssignedToDifferentTabs(NodeDefinition childDefinition, Entity entity) {
            UITab childTab = tab(childDefinition);
            UITab entityTab = tab(entity);
            return childTab == null || entityTab == null || childTab.getName().equals(entityTab.getName());
        }

        private UiEntityCollection createUiEntityCollection(EntityDefinition entityDefinition, Entity parentEntity) {
            UiEntityCollection uiEntityCollection = instantiateUiEntityCollection(entityDefinition, parentEntity);
            setRelevance(parentEntity, uiEntityCollection);
            List<Node<? extends NodeDefinition>> childrenEntities = parentEntity.getAll(entityDefinition.getName());
            for (Node<? extends NodeDefinition> childrenEntity : childrenEntities)
                addUiEntity((Entity) childrenEntity, uiEntityCollection);
            return uiEntityCollection;
        }

        private UITab tab(NodeDefinition nodeDefinition) {
            return survey.getUIOptions().getAssignedTab(nodeDefinition);
        }

        private UITab tab(Node<EntityDefinition> node) {
            return tab(node.getDefinition());
        }

        private List<UITab> rootTabs(Entity rootEntity) {
            return survey.getUIOptions().getAssignedRootTabSet(rootEntity.getDefinition()).getTabs();
        }

        private List<NodeDefinition> childDefinitions(Entity entity) {
            return nonDeprecated(entity.getDefinition().getChildDefinitions());
        }

        private List<NodeDefinition> tabAssociations(UITab tab) {
            return nonDeprecated(survey.getUIOptions().getNodesPerTab(tab, false));
        }

        private List<NodeDefinition> nonDeprecated(List<NodeDefinition> nodeDefinitions) {
            List<NodeDefinition> definitions = new ArrayList<NodeDefinition>();
            for (NodeDefinition definition : nodeDefinitions) {
                if (definition.getDeprecatedVersion() == null) // TODO: What if it's not deprecated in current version?
                    definitions.add(definition);
            }
            return definitions;
        }

        private EntityDefinition entityDefinition(UITab tab) { // TODO: Ugly, and might not hold
            List<NodeDefinition> definitions = tabAssociations(tab);
            if (definitions.isEmpty())
                return null;
            return definitions.get(0).getParentEntityDefinition();
        }

        private Node childNode(Entity entity, NodeDefinition childDefinition) {
            Node<? extends NodeDefinition> child = entity.getChild(childDefinition.getName());
            if (child == null)
                throw new IllegalStateException("Child not found in entity: " + entity + ". Expected to find " + childDefinition);
            return child;
        }

        private UiEntity instantiateUiEntity(Entity entity) {
            entity.setId(IdGenerator.nextId());
            UiEntity uiEntity = new UiEntity(entity.getId(), definitions.toDefinition(entity));
            setRelevance(entity.getParent(), uiEntity);
            return uiEntity;
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
            return AttributeConverter.toUiAttribute(definition, attribute);
        }

        private UiRecord instantiateUiRecord(Entity rootEntity, UiSurvey uiSurvey) {
            rootEntity.setId(IdGenerator.nextId());
            String name = rootEntity.getName();
            UiRecordCollection collection = uiSurvey.lookupRecordCollection(name);
            return new UiRecord(rootEntity.getId(), definitions.toDefinition(rootEntity), collection);
        }

        private UiInternalNode instantiateTabUiNode(UITab tab) {
            return new UiInternalNode(IdGenerator.nextId(), definitions.tabDefinition(tab));
        }

        private UiAttributeCollection instantiateUiAttributeCollection(AttributeDefinition attributeDefinition, Entity parentEntity) {
            return new UiAttributeCollection(
                    IdGenerator.nextId(),
                    parentEntity.getId(),
                    (UiAttributeCollectionDefinition) definitions.toCollectionDefinition(attributeDefinition)
            );
        }

        private UiEntityCollection instantiateUiEntityCollection(EntityDefinition entityDefinition, Entity parentEntity) {
            return new UiEntityCollection(
                    IdGenerator.nextId(),
                    parentEntity.getId(),
                    definitions.toCollectionDefinition(entityDefinition)
            );
        }
    }
}
