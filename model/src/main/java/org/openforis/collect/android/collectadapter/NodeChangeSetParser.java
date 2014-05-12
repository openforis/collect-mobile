package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.manager.MessageSource;
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
import org.openforis.idm.model.Node;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
class NodeChangeSetParser {
    private final NodeChangeSet nodeChangeSet;
    private final UiRecord uiRecord;
    private final MessageSource errorMessageSource = new ResourceBundleMessageSource();
    private final ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(errorMessageSource);

    public NodeChangeSetParser(NodeChangeSet nodeChangeSet, UiRecord uiRecord) {
        this.nodeChangeSet = nodeChangeSet;
        this.uiRecord = uiRecord;
    }

    public Map<UiAttribute, UiAttributeChange> extractChanges() {
        Map<UiAttribute, UiAttributeChange> attributeChanges = parseAttributeChanges();
        parseEntityChanges(attributeChanges);
        for (UiAttribute uiAttribute : attributeChanges.keySet()) {
            UiAttributeChange attributeChange = attributeChanges.get(uiAttribute);
            UiNode.Status newStatus = uiAttribute.determineStatus(attributeChange.validationErrors);
            attributeChange.statusChange = newStatus != uiAttribute.getStatus();
        }
        return attributeChanges;
    }

    private Map<UiAttribute, UiAttributeChange> parseAttributeChanges() {
        Map<UiAttribute, UiAttributeChange> attributeChanges = new HashMap<UiAttribute, UiAttributeChange>();
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange)
                parseAttributeChange((AttributeChange) nodeChange, attributeChanges);
        }
        return attributeChanges;
    }

    private void parseAttributeChange(AttributeChange attributeChange, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        UiAttribute uiAttribute = getUiAttribute(attributeChange);
        if (!attributeChanges.containsKey(uiAttribute))
            attributeChanges.put(uiAttribute, new UiAttributeChange());
        ValidationResults validationResults = attributeChange.getValidationResults();
        for (ValidationResult validationResult : validationResults.getFailed()) {
            if (!ignored(validationResult))
                addValidationError(toValidationError(attributeChange.getNode(), uiAttribute, validationResult), attributeChanges);
        }
    }

    private void parseEntityChanges(Map<UiAttribute, UiAttributeChange> attributeChanges) {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof EntityChange) {
                EntityChange entityChange = (EntityChange) nodeChange;
                for (Node<? extends NodeDefinition> childNode : entityChange.getNode().getChildren()) {
                    UiNode uiChildNode = uiRecord.lookupNode(childNode.getId());
                    if (uiChildNode instanceof UiAttribute) {
                        UiAttribute uiAttribute = (UiAttribute) uiChildNode;
                        parseRequiredValidationError(uiAttribute, entityChange, attributeChanges);
                        parseRelevance(uiAttribute, entityChange, attributeChanges);
                    }
                }
            }
        }
    }

    private void parseRequiredValidationError(UiAttribute uiAttribute, EntityChange entityChange, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(uiAttribute.getName());
        if (validationResultFlag != null && !validationResultFlag.isOk()) {
            String message = errorMessageSource.getMessage(Locale.getDefault(), "validation.requiredField");
            addValidationError(new UiValidationError(message, level(validationResultFlag), uiAttribute), attributeChanges);
        }
    }

    private void parseRelevance(UiAttribute uiAttribute, EntityChange entityChange, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        Boolean relevant = entityChange.getChildrenRelevance().get(uiAttribute.getName());
        if (relevant == null)
            return;
        UiAttributeChange attributeChange = attributeChanges.get(uiAttribute);
        if (attributeChange == null) {
            attributeChange = new UiAttributeChange();
            attributeChanges.put(uiAttribute, attributeChange);
        }
        attributeChange.relevanceChange = uiAttribute.isRelevant() != relevant;
    }


    private boolean ignored(ValidationResult validationResult) {
        return validationResult.getValidator() instanceof SpecifiedValidator;
    }

    private UiAttribute getUiAttribute(AttributeChange attributeChange) {
        Integer attributeId = attributeChange.getNode().getId();
        UiAttribute uiAttribute = (UiAttribute) uiRecord.lookupNode(attributeId);
        if (uiAttribute == null)
            throw new IllegalStateException("Attribute with id " + attributeId + " not found");
        return uiAttribute;
    }

    private void addValidationError(UiValidationError validationError, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        UiAttribute uiAttribute = validationError.getAttribute();
        UiAttributeChange attributeChange = attributeChanges.get(uiAttribute);
        if (attributeChange == null) {
            attributeChange = new UiAttributeChange();
            attributeChanges.put(uiAttribute, attributeChange);
        }
        attributeChange.validationErrors.add(validationError);
    }

    private UiValidationError toValidationError(Attribute attribute, UiAttribute uiAttribute, ValidationResult validationResult) {
        String message = validationMessageBuilder.getValidationMessage(attribute, validationResult, Locale.getDefault());
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
}
