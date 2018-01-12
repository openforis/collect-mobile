package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;

import java.util.*;

import static org.openforis.collect.android.collectadapter.AttributeUtils.*;

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
        Map<UiNode, UiNodeChange> nodeChanges = new HashMap<UiNode, UiNodeChange>();
        updateCalculatedAttributeValues(nodeChanges);
        parseNodeChanges(nodeChanges);
        determineStatusChanges(nodeChanges);
        return nodeChanges;
    }

    private void updateCalculatedAttributeValues(Map<UiNode, UiNodeChange> nodeChanges) {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange && isShown(nodeChange.getNode())) {
                Attribute attribute = ((AttributeChange) nodeChange).getNode();
                UiAttribute uiAttribute = getUiAttribute(nodeChange);
                if (uiAttribute == null)
                    return;
                if (uiAttribute.isCalculated()) {
                    AttributeConverter.updateUiValue(attribute, uiAttribute);
                    addNodeChange(uiAttribute, nodeChanges);
                }
            }
        }
    }

    private void determineStatusChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        for (UiNode uiNode : nodeChanges.keySet()) {
            UiNodeChange nodeChange = addNodeChange(uiNode, nodeChanges);
            UiNode.Status newStatus = uiNode.determineStatus(nodeChange.validationErrors);
            nodeChange.statusChange = newStatus != uiNode.getStatus();
        }
    }

    private void parseNodeChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange)
                parseValidationErrors((AttributeChange) nodeChange, nodeChanges);
            else if (nodeChange instanceof EntityChange) {
                parseRequiredValidation((EntityChange) nodeChange, nodeChanges);
                parseRelevanceChanges((EntityChange) nodeChange, nodeChanges);
            }
        }
    }

    private void parseRelevanceChanges(EntityChange entityChange, Map<UiNode, UiNodeChange> nodeChanges) {
        Entity parentNode = entityChange.getNode();
        UiInternalNode parentUiNode = (UiInternalNode) uiRecord.lookupNode(parentNode.getId());
        for (Map.Entry<String, Boolean> relevanceEntry : entityChange.getChildrenRelevance().entrySet()) {
            NodeDefinition nodeDefinition = parentNode.getDefinition().getChildDefinition(relevanceEntry.getKey());
            if (isHidden(nodeDefinition))
                continue;

            boolean relevant = relevanceEntry.getValue();
            for (UiNode uiNode : parentUiNode.findAllByName(nodeDefinition.getName())) {
                boolean previouslyRelevant = uiNode.isRelevant();
                if (relevant != previouslyRelevant)
                    addNodeChange(uiNode, nodeChanges).relevanceChange = true;

            }
        }
    }

    private void parseValidationErrors(AttributeChange attributeChange, Map<UiNode, UiNodeChange> nodeChanges) {
        Attribute<?, ?> node = attributeChange.getNode();
        if (isCalculated(node) || isHidden(node) || isIrrelevant(node))
            return;
        UiAttribute uiAttribute = getUiAttribute(attributeChange);
        if (uiAttribute == null)
            return;
        UiNodeChange nodeChange = addNodeChange(uiAttribute, nodeChanges);
        ValidationResultFlag requiredErrorResult = node.getParent().getMinCountValidationResult(node.getName());
        addRequiredValidationErrorIfAny(uiAttribute, nodeChanges, requiredErrorResult);

        ValidationResults validationResults = attributeChange.getValidationResults();
        List<UiValidationError> validationErrors = new ArrayList<UiValidationError>();
        for (ValidationResult validationResult : validationResults.getFailed()) {
            if (!ignored(validationResult))
                validationErrors.add(toValidationError(node, uiAttribute, validationResult));
        }
        if (!validationErrors.isEmpty())
            nodeChange.validationErrors.addAll(validationErrors);
    }

    private boolean isIrrelevant(Attribute<?, ?> node) {
        return !node.getParent().isRelevant(node.getName());
    }

    private void parseRequiredValidation(EntityChange entityChange, Map<UiNode, UiNodeChange> uiNodeChanges) {
        Entity entity = entityChange.getNode();
        UiEntity parentNode = (UiEntity) uiRecord.lookupNode(entity.getId());
        for (Map.Entry<String, ValidationResultFlag> validationEntry : entityChange.getChildrenMinCountValidation().entrySet()) {
            String childDefName = validationEntry.getKey();
            NodeDefinition childDef = entity.getDefinition().getChildDefinition(childDefName);
            ValidationResultFlag validationResultFlag = validationEntry.getValue();
            Collection<UiNode> childrenNodes = parentNode.findAllByName(childDefName);

            for (UiNode childNode : childrenNodes) {
                if (childDef instanceof AttributeDefinition && !((AttributeDefinition) childDef).isCalculated() && isShown(childDef)
                        || childNode instanceof UiEntityCollection) {
                    addRequiredValidationErrorIfAny(childNode, uiNodeChanges, validationResultFlag);
                }
            }
        }
        /*
        for (Node<? extends NodeDefinition> childNode : entity.getChildren()) {
            if (isCalculated(childNode) || isHidden(childNode) || childNode.getId() == null)
                continue;

            UiNode uiChildNode = uiRecord.lookupNode(childNode.getId());
            if (uiChildNode != null) {
                ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(uiChildNode.getName());
                addRequiredValidationErrorIfAny(uiChildNode, uiNodeChanges, validationResultFlag);
            }
        }
        */
    }

    private void addRequiredValidationErrorIfAny(UiNode uiNode, Map<UiNode, UiNodeChange> nodeChanges, ValidationResultFlag validationResultFlag) {
        if (validationResultFlag != null && !validationResultFlag.isOk()) {
            String message = messages.getMessage(this.locale, "validation.requiredField");
            UiNodeChange nodeChange = addNodeChange(uiNode, nodeChanges);
            if (!nodeChange.validationErrors.isEmpty())
                return; // We've already added required validation for this node
            nodeChange.validationErrors.add(new UiValidationError(message, level(validationResultFlag), uiNode));
        }
    }

    private boolean ignored(ValidationResult validationResult) {
        return validationResult.getValidator() instanceof SpecifiedValidator;
    }

    private UiAttribute getUiAttribute(NodeChange nodeChange) {
        Integer attributeId = nodeChange.getNode().getId();
        return (UiAttribute) uiRecord.lookupNode(attributeId);
    }

    private UiNodeChange addNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
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
