package org.openforis.collect.android.viewmodel;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class UiAttribute extends UiNode {
    private Set<UiValidationError> validationErrors;

    public UiAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public abstract boolean isEmpty();

    public Set<UiValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Set<UiValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void updateStatus(Set<UiValidationError> validationErrors) {
        UiNode.Status oldStatus = getStatus();
        UiNode.Status newStatus = determineStatus(validationErrors);
        if (oldStatus != newStatus) {
            updateStatusOfNodeAndParents(newStatus);
        }
    }

    private UiNode.Status determineStatus(Set<UiValidationError> validationErrors) {
        UiNode.Status newStatus;
        if (validationErrors.isEmpty())
            newStatus = isEmpty()
                    ? UiNode.Status.EMPTY
                    : UiNode.Status.OK;
        else {
            UiValidationError.Level level = getValidationErrorLevel(validationErrors);
            newStatus = level == UiValidationError.Level.WARNING
                    ? UiNode.Status.VALIDATION_WARNING
                    : UiNode.Status.VALIDATION_ERROR;
        }
        return newStatus;
    }


    private UiValidationError.Level getValidationErrorLevel(Set<UiValidationError> validationErrors) {
        UiValidationError.Level level = UiValidationError.Level.values()[0];
        for (UiValidationError validationError : validationErrors) {
            if (validationError.getLevel().ordinal() > level.ordinal())
                level = validationError.getLevel();
        }
        return level;
    }

}
