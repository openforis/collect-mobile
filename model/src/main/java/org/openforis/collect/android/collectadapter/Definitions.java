package org.openforis.collect.android.collectadapter;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.util.StringUtils;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UIFileAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiAttributeCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCoordinateDefinition;
import org.openforis.collect.android.viewmodel.UiEntityCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;
import org.openforis.collect.android.viewmodel.UiTaxonDefinition;
import org.openforis.collect.android.viewmodel.UiTextAttributeDefinition;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.Prompt;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class Definitions {
    private static final String COLLECTION_ID_PREFIX = "collection-";
    private static final String SURVEY_DEFINITION_ID = "survey";
    private final CollectSurvey collectSurvey;
    private final String preferredLanguageCode;
    private Map<String, Definition> definitionById = new HashMap<String, Definition>();
    private final List<UiSpatialReferenceSystem> spatialReferenceSystems;

    public Definitions(CollectSurvey collectSurvey, String preferredLanguageCode) {
        this.collectSurvey = collectSurvey;
        spatialReferenceSystems = createSpatialReferenceSystems(collectSurvey);
        this.preferredLanguageCode = preferredLanguageCode;
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
        String label = spatialReferenceSystem.getLabel(preferredLanguageCode, collectSurvey.getDefaultLanguage());
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
        String label = ObjectUtils.defaultIfNull(collectSurvey.getProjectName(preferredLanguageCode, true), "Project label");
        String surveyDescription = collectSurvey.getDescription(preferredLanguageCode, true);
        addDefinition(new Definition(SURVEY_DEFINITION_ID, collectSurvey.getName(), label, null, surveyDescription, null, null, true));
        List<EntityDefinition> rootEntityDefinitions = collectSurvey.getSchema().getRootEntityDefinitions();

        for (EntityDefinition entityDefinition : rootEntityDefinitions)
            addNodeDefinition(entityDefinition);

        for (Map.Entry<String, Definition> defEntry : definitionById.entrySet()) {
            Definition def = defEntry.getValue();
            if (NumberUtils.isNumber(def.id)) {
                int nodeDefId = Integer.parseInt(def.id);
                NodeDefinition nodeDef = collectSurvey.getSchema().getDefinitionById(nodeDefId);
                Set<NodeDefinition> relevanceSourceNodeDefs = nodeDef.getSurvey().getRelevanceSourceNodeDefinitions(nodeDef);
                for (NodeDefinition sourceNodeDef : relevanceSourceNodeDefs) {
                    Definition sourceDef = toDefinition(sourceNodeDef);
                    if (sourceDef != null) {
                        def.relevanceSources.add(sourceDef);
                    }
                }
            }
        }
    }

    private void addNodeDefinition(NodeDefinition nodeDefinition) {
        Definition definition = createDefinition(nodeDefinition);
        addDefinition(definition);
        if (nodeDefinition.isMultiple())
            addDefinition(createCollectionDefinition(nodeDefinition, definition));

        if (nodeDefinition instanceof EntityDefinition)
            for (NodeDefinition childDefinition : ((EntityDefinition) nodeDefinition).getChildDefinitions())
                addNodeDefinition(childDefinition);
    }

    private Definition createDefinition(NodeDefinition nodeDef) {
        String id = nodeDefinitionId(nodeDef);
        String name = nodeDef.getName();
        String label = label(nodeDef);
        String interviewLabel = nodeDef.getPrompt(Prompt.Type.INTERVIEW, preferredLanguageCode);
        Integer keyOfDefinitionId = getKeyOfDefinitionId(nodeDef);
        boolean required = isRequired(nodeDef);
        Definition def;
        if (nodeDef instanceof AttributeDefinition) {
            CollectSurvey survey = nodeDef.getSurvey();
            CollectAnnotations annotations = survey.getAnnotations();
            UIOptions uiOptions = survey.getUIOptions();
            boolean calculated = ((AttributeDefinition) nodeDef).isCalculated();
            boolean calculatedOnlyOneTime = annotations.isCalculatedOnlyOneTime(nodeDef);
            boolean hidden = uiOptions.isHidden(nodeDef);
            if (nodeDef instanceof TaxonAttributeDefinition)
                def = new UiTaxonDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        ((TaxonAttributeDefinition) nodeDef).getTaxonomy(),
                        nodeDescription(nodeDef), nodePrompt(nodeDef), interviewLabel, required);
            else if (nodeDef instanceof CoordinateAttributeDefinition) {
                CoordinateAttributeDefinition coordinateDefn = (CoordinateAttributeDefinition) nodeDef;
                def = new UiCoordinateDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        spatialReferenceSystems, nodeDescription(nodeDef),
                        nodePrompt(nodeDef), interviewLabel, required,
                        isDestinationPointSpecified(coordinateDefn),
                        annotations.isAllowOnlyDeviceCoordinate(coordinateDefn),
                        annotations.isIncludeCoordinateAltitude(coordinateDefn),
                        annotations.isIncludeCoordinateAccuracy(coordinateDefn));
            } else if (nodeDef instanceof CodeAttributeDefinition) {
                EntityDefinition parentDef = nodeDef.getParentEntityDefinition();
                boolean enumerator = !parentDef.isRoot() && parentDef.isEnumerable() && parentDef.isEnumerate()
                        && ((CodeAttributeDefinition) nodeDef).isKey();
                def = new UiCodeAttributeDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        nodeDescription(nodeDef), nodePrompt(nodeDef), interviewLabel, required,
                        uiOptions.getShowCode((CodeAttributeDefinition) nodeDef), enumerator);
            } else if (nodeDef instanceof FileAttributeDefinition) {
                CollectAnnotations.FileType fileType = annotations.getFileType((FileAttributeDefinition) nodeDef);
                def = new UIFileAttributeDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        nodeDescription(nodeDef), nodePrompt(nodeDef), interviewLabel, required, fileType);
            } else if (nodeDef instanceof TextAttributeDefinition) {
                CollectAnnotations.TextInput inputType = annotations.getTextInput((TextAttributeDefinition) nodeDef);
                boolean autoUppercase = uiOptions.isAutoUppercase((TextAttributeDefinition) nodeDef);
                UiTextAttributeDefinition uiTextAttributeDef = new UiTextAttributeDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        inputType, nodeDescription(nodeDef), nodePrompt(nodeDef), interviewLabel, required);
                uiTextAttributeDef.setAutoUppercase(autoUppercase);
                def = uiTextAttributeDef;
            } else
                def = new UiAttributeDefinition(id, name, label, keyOfDefinitionId,
                        calculated, calculatedOnlyOneTime, hidden,
                        nodeDescription(nodeDef), nodePrompt(nodeDef), interviewLabel, required);
        } else {
            def = new Definition(id, name, label, keyOfDefinitionId, nodeDescription(nodeDef),
                    nodePrompt(nodeDef), interviewLabel, required);
        }
        return def;
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
            EntityDefinition entityDef = (EntityDefinition) nodeDefinition;
            boolean enumerated = !entityDef.isRoot() && entityDef.isEnumerate() && entityDef.isEnumerable();
            return new UiEntityCollectionDefinition(
                    collectionNodeDefinitionId(nodeDefinition),
                    nodeDefinition.getName(),
                    collectionLabel(nodeDefinition),
                    getKeyOfDefinitionId(nodeDefinition),
                    nodeDescription(nodeDefinition),
                    nodePrompt(nodeDefinition),
                    isRequired(nodeDefinition),
                    enumerated,
                    entityDef.getFixedMinCount(),
                    entityDef.getFixedMaxCount());
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

    public static int extractOriginalDefinitionId(Definition def) {
        String id = def.id;
        String originalIdStr = id.startsWith(COLLECTION_ID_PREFIX) ? id.substring(COLLECTION_ID_PREFIX.length()) : id;
        int definitionId = Integer.parseInt(originalIdStr);
        return definitionId;
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
        return COLLECTION_ID_PREFIX + nodeDefinition.getId();
    }

    private String label(NodeDefinition nodeDefinition) {
        return nodeDefinition.getFailSafeLabel(preferredLanguageCode);
    }

    private String collectionLabel(NodeDefinition nodeDefinition) {
        String label = nodeDefinition.getLabel(NodeLabel.Type.HEADING, preferredLanguageCode);
        if (label == null) {
            label = nodeDefinition.getLabel(NodeLabel.Type.INSTANCE, preferredLanguageCode);
            if (label == null && !collectSurvey.isDefaultLanguage(preferredLanguageCode)) {
                label = nodeDefinition.getFailSafeLabel(NodeLabel.Type.HEADING, NodeLabel.Type.INSTANCE);
            }
        }
        return label;
    }

    private String nodeDescription(NodeDefinition nodeDefinition) {
        //TODO use latest core API
        return StringUtils.normalizeWhiteSpace(nodeDefinition.getDescription(preferredLanguageCode, true));
    }

    private String nodePrompt(NodeDefinition nodeDefinition) {
        return nodeDefinition.getFailSafePrompt(Prompt.Type.HANDHELD, preferredLanguageCode);
        //return StringUtils.normalizeWhiteSpace(prompts.get(0).getText());
    }
}
