package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiTaxonDefinition;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.*;
import org.openforis.idm.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class Definitions {
    private static final String SURVEY_DEFINITION_ID = "survey";
    private final CollectSurvey collectSurvey;
    private Map<String, Definition> definitionById = new HashMap<String, Definition>();

    public Definitions(CollectSurvey collectSurvey) {
        this.collectSurvey = collectSurvey;
        addSurveyDefinitions();
    }

    private void addSurveyDefinitions() {
        List<LanguageSpecificText> labels = collectSurvey.getProjectNames();
        String label = labels.isEmpty() ? "Survey" : labels.get(0).getText();
        addDefinition(
                new Definition(SURVEY_DEFINITION_ID, collectSurvey.getName(), label)
        );
        List<EntityDefinition> rootEntityDefinitions = collectSurvey.getSchema().getRootEntityDefinitions();

        for (EntityDefinition entityDefinition : rootEntityDefinitions)
            addNodeDefinition(entityDefinition);

        for (UITabSet tabSet : collectSurvey.getUIOptions().getTabSets())
            for (UITab tab : tabSet.getTabs())
                addTabDefinition(tab);
    }

    private void addTabDefinition(UITab tab) {
        addDefinition(
                new Definition(tabDefinitionId(tab), tab.getName(), label(tab))
        );

        if (tab.getTabs() != null)
            for (UITab childTab : tab.getTabs())
                addTabDefinition(childTab);
    }

    private void addNodeDefinition(NodeDefinition nodeDefinition) {
        addDefinition(createDefinition(nodeDefinition));
        if (nodeDefinition.isMultiple())
            addDefinition(createCollectionDefinition(nodeDefinition));

        if (nodeDefinition instanceof EntityDefinition)
            for (NodeDefinition childDefinition : ((EntityDefinition) nodeDefinition).getChildDefinitions())
                addNodeDefinition(childDefinition);
    }

    private Definition createDefinition(NodeDefinition nodeDefinition) {
        String id = nodeDefinitionId(nodeDefinition);
        String name = nodeDefinition.getName();
        String label = label(nodeDefinition);
        Integer keyOfDefinitionId = getKeyOfDefinitionId(nodeDefinition);
        if (nodeDefinition instanceof TaxonAttributeDefinition)
            return new UiTaxonDefinition(id, name, label, keyOfDefinitionId,
                    ((TaxonAttributeDefinition) nodeDefinition).getTaxonomy());
        else
            return new Definition(id, name, label, keyOfDefinitionId);
    }

    private Definition createCollectionDefinition(NodeDefinition nodeDefinition) {
        return new Definition(
                collectionNodeDefinitionId(nodeDefinition),
                nodeDefinition.getName(),
                collectionLabel(nodeDefinition),
                getKeyOfDefinitionId(nodeDefinition)
        );
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

    public Definition tabDefinition(UITab tab) {
        return definitionById(tabDefinitionId(tab));
    }

    public NodeDefinition toNodeDefinition(Definition definition) {
        try {
            return collectSurvey.getSchema().getDefinitionById(Integer.parseInt(definition.id));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Expected definition id to be an int, was " + definition.id);
        }
    }

    private String tabDefinitionId(UITab tab) {
        return "tab-" + tab.getName();
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

    private String label(UITab tab) {
        return tab.getLabels().get(0).getText(); // TODO: Take language and type into account
    }
}
