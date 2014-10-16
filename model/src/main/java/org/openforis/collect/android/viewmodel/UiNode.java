package org.openforis.collect.android.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class UiNode {
    private Set<UiValidationError> validationErrors;
    private final int id;
    private final Definition definition;
    private UiInternalNode parent;
    private Status status;
    private boolean relevant = true;

    public UiNode(int id, boolean relevant, Definition definition) {
        this.id = id;
        this.relevant = relevant;
        this.definition = definition;
        status = Status.OK;
    }

    public final void init() {
        if (parent != null)
            parent.register(this);
        if (this instanceof UiInternalNode) {
            for (UiNode child : ((UiInternalNode) this).getChildren())
                child.init();
        }
    }

    public int getId() {
        return id;
    }

    public Definition getDefinition() {
        return definition;
    }

    public String getName() {
        return definition.name;
    }

    public String getLabel() {
        return definition.label;
    }

    public UiInternalNode getParent() {
        return parent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isRelevant() {
        return relevant;
    }

    public void setRelevant(boolean relevant) {
        this.relevant = relevant;
    }

    public void updateStatusOfNodeAndParents(UiNode.Status status) {
        setStatus(status);
        updateStatusOfParents();
    }

    public void updateStatusOfParents() {
        UiInternalNode parentNode = getParent();
        if (parentNode == null)
            return;
        UiNode.Status newParentStatus = UiNode.Status.values()[0];
        for (UiNode child : parentNode.getChildren()) {
            if (child.getStatus().ordinal() > newParentStatus.ordinal())
                newParentStatus = child.getStatus();
        }
        if (newParentStatus != parentNode.getStatus())
            parentNode.updateStatusOfNodeAndParents(newParentStatus);
    }


    public UiNode.Status determineStatus(Set<UiValidationError> validationErrors) {
        UiNode.Status newStatus;
        if (validationErrors.isEmpty())
            newStatus = UiNode.Status.OK;
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

    public int getSiblingCount() {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildCount();
    }

    public UiNode getSiblingAt(int index) {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildAt(index);
    }

    public UiRecord getUiRecord() {  // TODO: Can this be removed or moved? This doesn't make sense for UiRecordCollection and UiSurvey
        if (this instanceof UiRecord)
            return (UiRecord) this;
        if (parent == null)
            return null;
        return parent.getUiRecord();
    }

    public UiSurvey getUiSurvey() {
        return (UiSurvey) getUiRecord().getParent().getParent();
    }

    public int getIndexInParent() {
        if (parent == null)
            throw new IllegalStateException("Parent is null");
        return parent.getChildIndex(id);
    }

    void setParent(UiInternalNode parent) {
        this.parent = parent;
    }


    public Set<UiValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Set<UiValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Collection<UiNode> findAllByName(String name) {
        ArrayList<UiNode> found = new ArrayList<UiNode>();
        if (definition.name.equals(name))
            found.add(this);
        if (this instanceof UiInternalNode)
            for (UiNode childNode : ((UiInternalNode) this).getChildren())
                found.addAll(childNode.findAllByName(name));
        return found;
    }

    public void updateStatus(Set<UiValidationError> validationErrors) {
        UiNode.Status oldStatus = getStatus();
        UiNode.Status newStatus = determineStatus(validationErrors);
        if (oldStatus != newStatus) {
            updateStatusOfNodeAndParents(newStatus);
        }
    }
    public String toString() {
        return id + ": " + definition;
    }

    public boolean excludeWhenNavigating() {
        return false;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiNode uiNode = (UiNode) o;
        return id == uiNode.id;

    }

    public int hashCode() {
        return id;
    }

    public void removeFromParent() {
        if (parent != null)
            parent.removeChild(this);
    }

    public static enum Status {
        OK, EMPTY, VALIDATION_WARNING, VALIDATION_ERROR;


        public boolean isWorseThen(Status status) {
            return ordinal() > status.ordinal();
        }
    }
}
