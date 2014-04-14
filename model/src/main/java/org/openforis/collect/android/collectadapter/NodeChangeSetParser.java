package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
class NodeChangeSetParser {
    private final NodeChangeSet nodeChangeSet;
    private final Attribute attribute;
    private final UiAttribute uiAttribute;
    private final MessageSource errorMessageSource = new ResourceBundleMessageSource();
    private final ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(errorMessageSource);

    public NodeChangeSetParser(NodeChangeSet nodeChangeSet, Attribute attribute, UiAttribute uiAttribute) {
        this.nodeChangeSet = nodeChangeSet;
        this.attribute = attribute;
        this.uiAttribute = uiAttribute;
    }

    public Map<UiAttribute, Set<UiValidationError>> parseErrors() {
        Map<UiAttribute, Set<UiValidationError>> validationErrors = attributeValidationErrors();
        UiValidationError requiredAttributeMissing = requiredAttributeMissing(); // TODO: Need to include required validation for related attributes too
        if (requiredAttributeMissing != null)
            addValidationError(requiredAttributeMissing, validationErrors);
        return validationErrors; // TODO: Need to contain attribute, and related attributes in map
    }

    private UiValidationError requiredAttributeMissing() {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof EntityChange) {
                EntityChange entityChange = (EntityChange) nodeChange;
                ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(uiAttribute.getName());
                if (validationResultFlag != null && !validationResultFlag.isOk()) {
                    String message = errorMessageSource.getMessage("validation.requiredField");
                    return new UiValidationError(message, level(validationResultFlag), uiAttribute);
                }
            }
        }
        return null;
    }

    private Map<UiAttribute, Set<UiValidationError>> attributeValidationErrors() {
        Map<UiAttribute, Set<UiValidationError>> errorsByAttribute = new HashMap<UiAttribute, Set<UiValidationError>>();
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange) {
                AttributeChange attributeChange = (AttributeChange) nodeChange;
                UiAttribute uiAttribute = getUiAttribute(attributeChange);
                if (!errorsByAttribute.containsKey(uiAttribute))
                    errorsByAttribute.put(uiAttribute, new HashSet<UiValidationError>());
                ValidationResults validationResults = attributeChange.getValidationResults();
                for (ValidationResult validationResult : validationResults.getFailed()) {
                    if (!ignored(validationResult))
                        addValidationError(toValidationError(uiAttribute, validationResult), errorsByAttribute);
                }
            }
        }
        return errorsByAttribute;
    }

    private boolean ignored(ValidationResult validationResult) {
        return validationResult.getValidator() instanceof SpecifiedValidator;
    }

    private UiAttribute getUiAttribute(AttributeChange attributeChange) {
        Integer attributeId = attributeChange.getNode().getId();
        UiAttribute uiAttribute = (UiAttribute) this.uiAttribute.getUiRecord().lookupNode(attributeId);
        if (uiAttribute == null)
            throw new IllegalStateException("Attribute with id " + attributeId + " not found");
        return uiAttribute;
    }

    private void addValidationError(UiValidationError validationError, Map<UiAttribute, Set<UiValidationError>> errorsByAttribute) {
        UiAttribute uiAttribute = validationError.getAttribute();
        Set<UiValidationError> errors = errorsByAttribute.get(uiAttribute);
        if (errors == null) {
            errors = new HashSet<UiValidationError>();
            errorsByAttribute.put(uiAttribute, errors);
        }
        errors.add(validationError);
    }

    private UiValidationError toValidationError(UiAttribute uiAttribute, ValidationResult validationResult) {
        String message = validationMessageBuilder.getValidationMessage(attribute, validationResult);
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