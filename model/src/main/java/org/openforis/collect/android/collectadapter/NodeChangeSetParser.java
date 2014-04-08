package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.AttributeValidationError;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

import java.util.HashSet;
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

    public Set<UiValidationError> parseErrors() {
        Set<UiValidationError> validationErrors = attributeValidationErrors();
        UiValidationError requiredAttributeMissing = requiredAttributeMissing();
        if (requiredAttributeMissing != null)
            validationErrors.add(requiredAttributeMissing);
        return validationErrors;
    }

    private UiValidationError requiredAttributeMissing() {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof EntityChange) {
                EntityChange entityChange = (EntityChange) nodeChange;
                ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(uiAttribute.getName());
                if (validationResultFlag != null && !validationResultFlag.isOk()) {
                    String message = errorMessageSource.getMessage("validation.requiredField");
                    return new AttributeValidationError(message, level(validationResultFlag), uiAttribute);
                }
            }
        }
        return null;
    }

    private Set<UiValidationError> attributeValidationErrors() {
        Set<UiValidationError> errors = new HashSet<UiValidationError>();
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange) {
                AttributeChange attributeChange = (AttributeChange) nodeChange;
                ValidationResults validationResults = attributeChange.getValidationResults();
                for (ValidationResult validationResult : validationResults.getFailed()) {
                    errors.add(toAttributeValidationError(validationResult));
                }
            }
        }
        return errors;
    }

    private UiValidationError toAttributeValidationError(ValidationResult validationResult) {
        String message = validationMessageBuilder.getValidationMessage(attribute, validationResult);
        return new AttributeValidationError(message, getLevel(validationResult), uiAttribute); // TODO: Need to capture error message
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
