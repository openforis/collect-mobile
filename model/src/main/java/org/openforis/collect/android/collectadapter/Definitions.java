package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.util.StringUtils;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.*;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.model.Node;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class Definitions {
    private static final String SURVEY_DEFINITION_ID = "survey";
    private final CollectSurvey collectSurvey;
    private Map<String, Definition> definitionById = new HashMap<String, Definition>();
    private final List<UiSpatialReferenceSystem> spatialReferenceSystems;

    public Definitions(CollectSurvey collectSurvey) {
        this.collectSurvey = collectSurvey;
        spatialReferenceSystems = createSpatialReferenceSystems(collectSurvey);
        addSurveyDefinitions();
    }

    private List<UiSpatialReferenceSystem> createSpatialReferenceSystems(CollectSurvey collectSurvey) {
        List<UiSpatialReferenceSystem> uiSpatialReferenceSystems = new ArrayList<UiSpatialReferenceSystem>();
        for (SpatialReferenceSystem spatialReferenceSystem : collectSurvey.getSpatialReferenceSystems()) {
            uiSpatialReferenceSystems.add(
                    new UiSpatialReferenceSystem(
                            spatialReferenceSystem.getId(),
                            spatialReferenceSystem.getWellKnownText(),
                            label(spatialReferenceSystem))
            );
        }
        return uiSpatialReferenceSystems;
    }

    private String label(SpatialReferenceSystem spatialReferenceSystem) {
        String label = spatialReferenceSystem.getLabel(Locale.getDefault().getLanguage());
        if (label == null) {
            List<LanguageSpecificText> labels = spatialReferenceSystem.getLabels();
            if (!labels.isEmpty())
                label = labels.get(0).getText();
            if (label == null)
                label = spatialReferenceSystem.getId();
        }
        return label;
    }

    private void addSurveyDefinitions() {
        List<LanguageSpecificText> labels = collectSurvey.getProjectNames();
        String label = labels.isEmpty() ? "Survey" : labels.get(0).getText();
        String surveyDescription = collectSurvey.getDescription(); // TODO: Take language into account
        addDefinition(
                new Definition(SURVEY_DEFINITION_ID, collectSurvey.getName(), label, null, surveyDescription, null, true)
        );
        List<EntityDefinition> rootEntityDefinitions = collectSurvey.getSchema().getRootEntityDefinitions();

        for (EntityDefinition entityDefinition : rootEntityDefinitions)
            addNodeDefinition(entityDefinition);
    }

    private void addNodeDefinition(NodeDefinition nodeDefinition) {
        if (AttributeUtils.isHidden(collectSurvey, nodeDefinition))
            return;
        Definition definition = createDefinition(nodeDefinition);
        addDefinition(definition);
        if (nodeDefinition.isMultiple())
            addDefinition(createCollectionDefinition(nodeDefinition, definition));

        if (nodeDefinition instanceof EntityDefinition)
            for (NodeDefinition childDefinition : ((EntityDefinition) nodeDefinition).getChildDefinitions())
                addNodeDefinition(childDefinition);
    }

    private Definition createDefinition(NodeDefinition nodeDefinition) {
        String id = nodeDefinitionId(nodeDefinition);
        String name = nodeDefinition.getName();
        String label = label(nodeDefinition);
        Integer keyOfDefinitionId = getKeyOfDefinitionId(nodeDefinition);
        boolean required = isRequired(nodeDefinition);
        if (nodeDefinition instanceof AttributeDefinition) {
            boolean calculated = ((AttributeDefinition) nodeDefinition).isCalculated();
            if (nodeDefinition instanceof TaxonAttributeDefinition)
                return new UiTaxonDefinition(id, name, label, keyOfDefinitionId, calculated,
                        ((TaxonAttributeDefinition) nodeDefinition).getTaxonomy(),
                        nodeDescription(nodeDefinition), nodePrompt(nodeDefinition), required);
            else if (nodeDefinition instanceof CoordinateAttributeDefinition) {
                CoordinateAttributeDefinition coordinateDefn = (CoordinateAttributeDefinition) nodeDefinition;
                return new UiCoordinateDefinition(id, name, label, keyOfDefinitionId, calculated,
                        spatialReferenceSystems, nodeDescription(nodeDefinition),
                        nodePrompt(nodeDefinition), required,
                        isDestinationPointSpecified(coordinateDefn),
                        collectSurvey.getAnnotations().isAllowOnlyDeviceCoordinate(coordinateDefn));
            } else if (nodeDefinition instanceof CodeAttributeDefinition) {
                boolean enumerator = nodeDefinition.getParentEntityDefinition().isEnumerable() && ((CodeAttributeDefinition) nodeDefinition).isKey();
                return new UiCodeAttributeDefinition(id, name, label, keyOfDefinitionId, calculated,
                        nodeDescription(nodeDefinition), nodePrompt(nodeDefinition), required,
                        collectSurvey.getUIOptions().getShowCode((CodeAttributeDefinition) nodeDefinition), enumerator);
            } else
                return new UiAttributeDefinition(id, name, label, keyOfDefinitionId, calculated,
                        nodeDescription(nodeDefinition), nodePrompt(nodeDefinition), required);
        } else {
            return new Definition(id, name, label, keyOfDefinitionId, nodeDescription(nodeDefinition),
                    nodePrompt(nodeDefinition), required);
        }
    }

    private boolean isDestinationPointSpecified(CoordinateAttributeDefinition nodeDefinition) {
        boolean destinationPointSpecified = false;
        for (Check<?> check : nodeDefinition.getChecks()) {
            destinationPointSpecified = check instanceof DistanceCheck
                    && ((DistanceCheck) check).getDestinationPointExpression() != null;
        }
        return destinationPointSpecified;
    }

    private boolean isRequired(NodeDefinition nodeDefinition) {
        return nodeDefinition.isAlwaysRequired(); // TODO: What about min count?
    }

    private Definition createCollectionDefinition(NodeDefinition nodeDefinition, Definition childDefinition) {
        if (nodeDefinition instanceof AttributeDefinition) {
            return new UiAttributeCollectionDefinition(
                    collectionNodeDefinitionId(nodeDefinition),
                    nodeDefinition.getName(),
                    collectionLabel(nodeDefinition),
                    AttributeConverter.getUiAttributeType(nodeDefinition),
                    (UiAttributeDefinition) childDefinition, isRequired(nodeDefinition));
        } else {
            boolean enumerated = ((EntityDefinition) nodeDefinition).isEnumerable();
            return new UiEntityCollectionDefinition(
                    collectionNodeDefinitionId(nodeDefinition),
                    nodeDefinition.getName(),
                    collectionLabel(nodeDefinition),
                    getKeyOfDefinitionId(nodeDefinition),
                    nodeDescription(nodeDefinition),
                    nodePrompt(nodeDefinition),
                    isRequired(nodeDefinition),
                    enumerated);
        }
    }

    private Integer getKeyOfDefinitionId(NodeDefinition nodeDefinition) {
        if (nodeDefinition instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDefinition).isKey())
            return nodeDefinition.getParentDefinition().getId();
        return null;
    }

    private void addDefinition(Definition definition) {
        definitionById.put(definition.id, definition);
    }

    public Definition schemaDefinition() {
        return definitionById(SURVEY_DEFINITION_ID);
    }

    public Definition definitionById(String id) {
        return definitionById.get(id);
    }

    public Definition toDefinition(NodeDefinition nodeDefinition) {
        return definitionById(nodeDefinitionId(nodeDefinition));
    }

    public Definition toCollectionDefinition(NodeDefinition nodeDefinition) {
        return definitionById(collectionNodeDefinitionId(nodeDefinition));
    }

    public Definition toDefinition(Node node) {
        return toDefinition(node.getDefinition());
    }

    public NodeDefinition toNodeDefinition(Definition definition) {
        try {
            return collectSurvey.getSchema().getDefinitionById(Integer.parseInt(definition.id));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Expected definition id to be an int, was " + definition.id);
        }
    }

    private String nodeDefinitionId(NodeDefinition nodeDefinition) {
        return String.valueOf(nodeDefinition.getId());
    }

    private String collectionNodeDefinitionId(NodeDefinition nodeDefinition) {
        return "collection-" + nodeDefinition.getId();
    }

    private String label(NodeDefinition nodeDefinition) { // TODO: Take language into account
        if (nodeDefinition.getLabels().isEmpty()) return null;
        String label = nodeDefinition.getLabel(NodeLabel.Type.INSTANCE);
        label = label == null ? nodeDefinition.getLabel(NodeLabel.Type.HEADING) : label;
        label = label == null ? nodeDefinition.getLabels().get(0).getText() : label;
        return label;
    }

    private String collectionLabel(NodeDefinition nodeDefinition) { // TODO: Take language into account
        String label = nodeDefinition.getLabel(NodeLabel.Type.HEADING);
        return label == null ? label(nodeDefinition) : label;
    }

    private String nodeDescription(NodeDefinition nodeDefinition) {
        return StringUtils.normalizeWhiteSpace(nodeDefinition.getDescription()); // TODO: Take language into account
    }

    private String nodePrompt(NodeDefinition nodeDefinition) {
        List<Prompt> prompts = nodeDefinition.getPrompts(); // TODO: Take language and type into account
        if (prompts == null || prompts.isEmpty())
            return null;
        return StringUtils.normalizeWhiteSpace(prompts.get(0).getText());
    }
}
