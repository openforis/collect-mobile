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
    private final Locale preferredLocale;

    public NodeChangeSetParser(NodeChangeSet nodeChangeSet, UiRecord uiRecord, String preferredLanguage) {
        this.nodeChangeSet = nodeChangeSet;
        this.uiRecord = uiRecord;
        this.preferredLocale = new Locale(preferredLanguage);
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
            if (nodeChange instanceof AttributeChange) {
                Attribute attribute = ((AttributeChange) nodeChange).getNode();
                UiAttribute uiAttribute = getUiAttribute(nodeChange);
                if (uiAttribute == null)
                    return;
                if (uiAttribute.isCalculated()) {
                    AttributeConverter.updateUiValue(attribute, uiAttribute);
                    getOrAddNodeChange(uiAttribute, nodeChanges);
                }
            }
        }
    }

    private void determineStatusChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        for (UiNode uiNode : nodeChanges.keySet()) {
            UiNodeChange nodeChange = getOrAddNodeChange(uiNode, nodeChanges);
            UiNode.Status newStatus = uiNode.determineStatus(nodeChange.validationErrors);
            nodeChange.statusChange = newStatus != uiNode.getStatus();
        }
    }

    private void parseNodeChanges(Map<UiNode, UiNodeChange> nodeChanges) {
        for (NodeChange<?> nodeChange : nodeChangeSet.getChanges()) {
            if (nodeChange instanceof AttributeChange)
                parseAttributeChange((AttributeChange) nodeChange, nodeChanges);
            else if (nodeChange instanceof EntityChange) {
                parseMinCountValidation((EntityChange) nodeChange, nodeChanges);
                parseMaxCountValidation((EntityChange) nodeChange, nodeChanges);
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
                    getOrAddNodeChange(uiNode, nodeChanges).relevanceChange = true;
            }
        }
    }

    private void parseAttributeChange(AttributeChange attributeChange, Map<UiNode, UiNodeChange> nodeChanges) {
        Attribute<?, ?> attribute = attributeChange.getNode();
        if (isCalculated(attribute) || isHidden(attribute) || isIrrelevant(attribute))
            return;
        UiAttribute uiAttribute = getUiAttribute(attributeChange);
        if (uiAttribute == null)
            return;

        UiNodeChange nodeChange = getOrAddNodeChange(uiAttribute, nodeChanges);
        if (attributeChange.getUpdatedFieldValues() != null) {
            AttributeConverter.updateUiValue(attribute, uiAttribute);
            nodeChange.valueChange = true;
        }

        ValidationResultFlag requiredErrorResult = attribute.getParent().getMinCountValidationResult(attribute.getName());
        addCountValidationErrorIfAny(uiAttribute, nodeChanges, requiredErrorResult, attribute.getParent().getMinCount(attribute.getDefinition()),
                "validation.requiredField", "validation.minCount");

        ValidationResults validationResults = attributeChange.getValidationResults();
        List<UiValidationError> validationErrors = new ArrayList<UiValidationError>();
        for (ValidationResult validationResult : validationResults.getFailed()) {
            if (!ignored(validationResult))
                validationErrors.add(toValidationError(attribute, uiAttribute, validationResult));
        }
        if (!validationErrors.isEmpty())
            nodeChange.validationErrors.addAll(validationErrors);
    }

    private boolean isIrrelevant(Attribute<?, ?> node) {
        return !node.getParent().isRelevant(node.getName());
    }

    private void parseMinCountValidation(EntityChange entityChange, Map<UiNode, UiNodeChange> uiNodeChanges) {
        parseMinMaxCountValidation(entityChange, true, uiNodeChanges);
    }

    private void parseMaxCountValidation(EntityChange entityChange, Map<UiNode, UiNodeChange> uiNodeChanges) {
        parseMinMaxCountValidation(entityChange, false, uiNodeChanges);
    }

    private void parseMinMaxCountValidation(EntityChange entityChange, boolean min, Map<UiNode, UiNodeChange> uiNodeChanges) {
        Entity entity = entityChange.getNode();
        UiEntity parentNode = (UiEntity) uiRecord.lookupNode(entity.getId());
        Map<String, ValidationResultFlag> childrenValidation = entityChange.getChildrenMinCountValidation();
        for (Map.Entry<String, ValidationResultFlag> validationEntry : childrenValidation.entrySet()) {
            String childDefName = validationEntry.getKey();
            NodeDefinition childDef = entity.getDefinition().getChildDefinition(childDefName);
            ValidationResultFlag validationResultFlag = validationEntry.getValue();
            Collection<UiNode> childrenNodes = parentNode.findAllByName(childDefName);

            for (UiNode childNode : childrenNodes) {
                if (childDef instanceof AttributeDefinition && !((AttributeDefinition) childDef).isCalculated() && isShown(childDef)
                        || childNode instanceof UiEntityCollection) {
                    if (validationResultFlag.isError()) {
                        Integer requiredCount = min ? entity.getMinCount(childDef) : entity.getMaxCount(childDef);
                        if (requiredCount != null && requiredCount > 0) {
                            if (min) {
                                addCountValidationErrorIfAny(childNode, uiNodeChanges, validationResultFlag, requiredCount,
                                        "validation.requiredField", "validation.minCount");
                            } else {
                                addCountValidationErrorIfAny(childNode, uiNodeChanges, validationResultFlag, requiredCount,
                                        "validation.maxCount", "validation.maxCount");
                            }
                        }
                    } else if (validationResultFlag.isOk()) {
                        //reset validation error
                        UiNodeChange change = getOrAddNodeChange(childNode, uiNodeChanges);
                        if (change.validationErrors == null && childNode.hasValidationErrors()) {
                            change.validationErrors = Collections.emptySet();
                        }
                    }
                }
            }
        }
    }

    private void addCountValidationErrorIfAny(UiNode uiNode, Map<UiNode, UiNodeChange> nodeChanges,
                                              ValidationResultFlag validationResultFlag, Integer requiredCount,
                                              String singleCountMessageKey, String multipleCountMessageKey) {
        if (validationResultFlag != null && !validationResultFlag.isOk()) {
            String message = requiredCount == null || requiredCount == 1 ? messages.getMessage(this.preferredLocale, singleCountMessageKey):
                    messages.getMessage(this.preferredLocale, multipleCountMessageKey, requiredCount);
            UiNodeChange nodeChange = getOrAddNodeChange(uiNode, nodeChanges);
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

    private UiNodeChange getOrAddNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        UiNodeChange nodeChange = nodeChanges.get(node);
        if (nodeChange == null) {
            nodeChange = new UiNodeChange();
            nodeChanges.put(node, nodeChange);
        }
        return nodeChange;
    }

    private UiValidationError toValidationError(Attribute attribute, UiAttribute uiAttribute, ValidationResult validationResult) {
        String message = validationMessageBuilder.getValidationMessage(attribute, validationResult, this.preferredLocale);
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
