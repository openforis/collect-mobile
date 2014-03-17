package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
class NodeChangeSetParser {
    private final NodeChangeSet nodeChangeSet;
    private final UiAttribute attribute;

    public NodeChangeSetParser(NodeChangeSet nodeChangeSet, UiAttribute attribute) {
        this.nodeChangeSet = nodeChangeSet;
        this.attribute = attribute;
    }

    // TODO: Handle other constraint violations
    public Set<UiValidationError> parseErrors() {
        Set<UiValidationError> validationErrors = new HashSet<UiValidationError>();
        UiValidationError requiredAttributeMissing = requiredAttributeMissing();
        if (requiredAttributeMissing != null)
            validationErrors.add(requiredAttributeMissing);
        return validationErrors;
    }

    private UiValidationError requiredAttributeMissing() {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof EntityChange) {
                EntityChange entityChange = (EntityChange) nodeChange;
                ValidationResultFlag validationResultFlag = entityChange.getChildrenMinCountValidation().get(attribute.getName());
                if (validationResultFlag != null && !validationResultFlag.isOk())
                    return new UiValidationError.RequiredAttributeMissing(level(validationResultFlag), attribute);
            }
        }
        return null;
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
