package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiIntegerAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.model.IntegerAttribute;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
public class IntegerConverter extends AttributeConverter<IntegerAttribute, UiIntegerAttribute> {
    public UiIntegerAttribute uiAttribute(UiAttributeDefinition definition, IntegerAttribute attribute) {
        UiIntegerAttribute a = new UiIntegerAttribute(attribute.getId(), isRelevant(attribute), definition);
        updateUiAttributeValue(attribute, a);
        return a;
    }

    protected void updateUiAttributeValue(IntegerAttribute attribute, UiIntegerAttribute uiAttribute) {
        uiAttribute.setValue(attribute.getValue().getValue()); // TODO: Set unit
    }

    protected UiIntegerAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiIntegerAttribute a = new UiIntegerAttribute(nodeDto.id, nodeDto.relevant, definition);
        a.setValue(nodeDto.intValue); // TODO: Set unit
        return a;
    }

    protected NodeDto dto(UiIntegerAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.intValue = uiAttribute.getValue();
        return dto;
    }

    public Value value(UiIntegerAttribute uiAttribute) {
        return new IntegerValue(uiAttribute.getValue(), null); // TODO: Pass unit - needs survey!
    }

    protected IntegerAttribute attribute(UiIntegerAttribute uiAttribute, NodeDefinition definition) {
        IntegerAttribute a = new IntegerAttribute((NumberAttributeDefinition) definition);
        a.setValue((IntegerValue) value(uiAttribute));
        return a;
    }
}
