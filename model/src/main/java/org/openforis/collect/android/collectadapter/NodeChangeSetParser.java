package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

import static org.openforis.collect.android.collectadapter.CalculatedAttributeUtils.isCalculated;

/**
 * @author Daniel Wiell
 */
class NodeChangeSetParser {
    private final NodeChangeSet nodeChangeSet;
    private final UiRecord uiRecord;
    private final Messages messages = new Messages();
    private final ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messages);
    private final Locale locale;

    public NodeChangeSetParser(NodeChangeSet nodeChangeSet, UiRecord uiRecord) {
        this.nodeChangeSet = nodeChangeSet;
        this.uiRecord = uiRecord;
        this.locale = Locale.getDefault();
    }

    public Map<UiNode, UiNodeChange> extractChanges() {
        Map<UiNode, UiNodeChange> nodeChanges = parseNodeChanges();
        determineStatusChanges(nodeChanges);
        return nodeChanges;
    }

    private void determineStatusChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        for (UiNode uiNode : nodeChanges.keySet()) {
            UiNodeChange nodeChange = getOrCreateUiNodeChange(uiNode, nodeChanges);
            UiNode.Status newStatus = uiNode.determineStatus(nodeChange.validationErrors);
            nodeChange.statusChange = newStatus != uiNode.getStatus();
        }
    }

    private Map<UiNode, UiNodeChange> parseNodeChanges() {
        Map<UiNode, UiNodeChange> nodeChanges = new HashMap<UiNode, UiNodeChange>();
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange)
                parseValidationErrors((AttributeChange) nodeChange, nodeChanges);
            else if (nodeChange instanceof EntityChange) {
                parseRequiredValidation((EntityChange) nodeChange, nodeChanges);
                parseRelevanceChanges((EntityChange) nodeChange, nodeChanges);
            }
        }
        return nodeChanges;
    }

    private void parseRelevanceChanges(EntityChange entityChange, Map<UiNode, UiNodeChange> nodeChanges) {
        Entity parentNode = entityChange.getNode();
        UiInternalNode parentUiNode = (UiInternalNode) uiRecord.lookupNode(parentNode.getId());
        for (Map.Entry<String, Boolean> relevanceEntry : entityChange.getChildrenRelevance().entrySet()) {
            NodeDefinition nodeDefinition = parentNode.getDefinition().getChildDefinition(relevanceEntry.getKey());
            if (isCalculated(nodeDefinition)) // TODO: Should we actually ignore calculated attributes?
                continue;

            boolean relevant = relevanceEntry.getValue();

            for (UiNode uiNode : parentUiNode.findAllByName(nodeDefinition.getName())) {
                boolean previouslyRelevant = uiNode.isRelevant();
                if (relevant != previouslyRelevant)
                    getOrCreateUiNodeChange(uiNode, nodeChanges).relevanceChange = true;

            }
        }
    }

    private void parseValidationErrors(AttributeChange attributeChange, Map<UiNode, UiNodeChange> nodeChanges) {
        if (isCalculated(attributeChange.getNode()))
            return; // TODO: Should we actually ignore calculated attributes?
        UiAttribute uiAttribute = getUiAttribute(attributeChange);
        UiNodeChange nodeChange = getOrCreateUiNodeChange(uiAttribute, nodeChanges);
        ValidationResults validationResults = attributeChange.getValidationResults();
        for (ValidationResult validationResult : validationResults.getFailed()) {
            if (!ignored(validationResult))
                nodeChange.validationErrors.add(toValidationError(attributeChange.getNode(), uiAttribute, validationResult));
        }
    }

    private void parseRequiredValidation(EntityChange entityChange, Map<UiNode, UiNodeChange> uiNodeChanges) {
        for (Node<? extends NodeDefinition> childNode : entityChange.getNode().getChildren()) {
            if (isCalculated(childNode))
                continue; // TODO: Should we actually ignore calculated attributes?
            UiNode uiChildNode = uiRecord.lookupNode(childNode.getId());
            parseRequiredValidationError(uiChildNode, entityChange, uiNodeChanges);
        }
    }

    private void parseRequiredValidationError(UiNode uiNode, EntityChange entityChange, Map<UiNode, UiNodeChange> nodeChanges) {
        ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(uiNode.getName());
        if (validationResultFlag != null && !validationResultFlag.isOk()) {
            String message = messages.getMessage(this.locale, "validation.requiredField");
            UiNodeChange nodeChange = getOrCreateUiNodeChange(uiNode, nodeChanges);
            nodeChange.validationErrors.add(new UiValidationError(message, level(validationResultFlag), uiNode));
        }
    }

    private boolean ignored(ValidationResult validationResult) {
        return validationResult.getValidator() instanceof SpecifiedValidator;
    }

    private UiAttribute getUiAttribute(NodeChange nodeChange) {
        Integer attributeId = nodeChange.getNode().getId();
        UiAttribute uiAttribute = (UiAttribute) uiRecord.lookupNode(attributeId);
        if (uiAttribute == null)
            throw new IllegalStateException("Attribute with id " + attributeId + " not found");
        return uiAttribute;
    }

    private UiNodeChange getOrCreateUiNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        UiNodeChange nodeChange = nodeChanges.get(node);
        if (nodeChange == null) {
            nodeChange = new UiNodeChange();
            nodeChanges.put(node, nodeChange);
        }
        return nodeChange;
    }

    private UiValidationError toValidationError(Attribute attribute, UiAttribute uiAttribute, ValidationResult validationResult) {
        String message = validationMessageBuilder.getValidationMessage(attribute, validationResult, this.locale);
        return new UiValidationError(message, getLevel(validationResult), uiAttribute);
    }

    private UiValidationError.Level getLevel(ValidationResult validationResult) {
        return validationResult.getFlag() == ValidationResultFlag.ERROR ? UiValidationError.Level.ERROR : UiValidationError.Level.WARNING;
    }

    private UiValidationError.Level level(ValidationResultFlag validationResultFlag) {
        switch (validationResultFlag) {
            case ERROR:
                return UiValidationError.Level.ERROR;
            case WARNING:
                return UiValidationError.Level.WARNING;
            default:
                throw new IllegalStateException("Cannot create validation error level from an OK validation result flag");
        }
    }

    private static class Messages extends ResourceBundleMessageSource {
        protected PropertyResourceBundle findBundle(Locale locale, String baseName) {
            return (PropertyResourceBundle) PropertyResourceBundle.getBundle(baseName, locale);
        }
    }
}
