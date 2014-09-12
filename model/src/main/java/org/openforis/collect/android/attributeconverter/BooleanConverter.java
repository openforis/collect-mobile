package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
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
    protected UiBooleanAttribute uiAttribute(UiAttributeDefinition definition, BooleanAttribute attribute) {
        UiBooleanAttribute uiAttribute = new UiBooleanAttribute(attribute.getId(), isRelevant(attribute), definition);
        updateUiAttributeValue(attribute, uiAttribute);
        return uiAttribute;
    }

    protected void updateUiAttributeValue(BooleanAttribute attribute, UiBooleanAttribute uiAttribute) {
        uiAttribute.setValue(attribute.getValue().getValue());
    }

    protected UiBooleanAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiBooleanAttribute uiAttribute = new UiBooleanAttribute(nodeDto.id, nodeDto.relevant, definition);
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
