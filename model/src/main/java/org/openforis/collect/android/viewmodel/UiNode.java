package org.openforis.collect.android.viewmodel;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

import java.util.*;

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
    private Date createdOn = new Date();
    private Date modifiedOn = new Date();

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

    public boolean isCalculated() {
        return false;
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

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public List<UiNode> updateStatusOfNodeAndParents(UiNode.Status status) {
        setStatus(status);
        List<UiNode> nodesWithUpdatedStatus = new ArrayList<UiNode>();
        nodesWithUpdatedStatus.add(this);
        nodesWithUpdatedStatus.addAll(updateStatusOfParents());
        return nodesWithUpdatedStatus;
    }

    public List<UiNode> updateStatusOfParents() {
        UiInternalNode parentNode = getParent();
        if (parentNode == null)
            return Collections.emptyList();
        UiNode.Status newParentStatus = UiNode.Status.values()[0];
        for (UiNode child : parentNode.getChildren()) {
            if (child.isRelevant() && child.getStatus().ordinal() > newParentStatus.ordinal())
                newParentStatus = child.getStatus();
        }
        if (newParentStatus != parentNode.getStatus()) {
            return parentNode.updateStatusOfNodeAndParents(newParentStatus);
        }
        return Collections.emptyList();
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

    public UiNode getRelevantSiblingAt(int index) {
        List<UiNode> relevantSiblings = getRelevantSiblings();
        return relevantSiblings.get(index);
    }

    public List<UiNode> getRelevantSiblings() {
        List<UiNode> siblings = parent.getChildren();
        List<UiNode> relevantSiblings = new ArrayList<UiNode>(siblings);
        CollectionUtils.filter(relevantSiblings, new Predicate<UiNode>() {
            public boolean evaluate(UiNode item) {
                return item.isRelevant();
            }
        });
        return relevantSiblings;
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
        status = determineStatus(validationErrors);
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

    public enum Status {
        OK, EMPTY, PENDING_VALIDATION, VALIDATION_WARNING, VALIDATION_ERROR;


        public boolean isWorseThen(Status status) {
            return ordinal() > status.ordinal();
        }
    }
}
