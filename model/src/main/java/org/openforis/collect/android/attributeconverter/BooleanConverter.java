package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiBooleanAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class BooleanConverter extends AttributeConverter<BooleanAttribute, UiBooleanAttribute> {
    protected UiBooleanAttribute uiAttribute(Definition definition, BooleanAttribute attribute) {
        UiBooleanAttribute uiAttribute = new UiBooleanAttribute(attribute.getId(), definition);
        Boolean value = attribute.getValue().getValue();
        uiAttribute.setValue(value == null ? false : value);
        return uiAttribute;
    }

    protected UiBooleanAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiBooleanAttribute uiAttribute = new UiBooleanAttribute(nodeDto.id, definition
        );
        uiAttribute.setValue(nodeDto.booleanValue);
        return uiAttribute;
    }

    protected NodeDto dto(UiBooleanAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.booleanValue = uiAttribute.getValue();
        return dto;
    }

    protected Value value(UiBooleanAttribute uiAttribute) {
        return new BooleanValue(uiAttribute.getValue());
    }

    protected BooleanAttribute attribute(UiBooleanAttribute uiAttribute, NodeDefinition definition) {
        BooleanAttribute a = new BooleanAttribute((BooleanAttributeDefinition) definition);
        a.setValue((BooleanValue) value(uiAttribute));
        return a;
    }
}
