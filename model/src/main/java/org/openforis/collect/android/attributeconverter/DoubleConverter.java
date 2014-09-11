package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiDoubleAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
// TODO: Set precision
public class DoubleConverter extends AttributeConverter<RealAttribute, UiDoubleAttribute> {
    public UiDoubleAttribute uiAttribute(Definition definition, RealAttribute attribute) {
        UiDoubleAttribute a = new UiDoubleAttribute(attribute.getId(), isRelevant(attribute), definition);
        a.setValue(attribute.getValue().getValue()); // TODO: Set unit
        return a;
    }

    protected UiDoubleAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiDoubleAttribute a = new UiDoubleAttribute(nodeDto.id, nodeDto.relevant, definition);
        a.setValue(nodeDto.doubleValue); // TODO: Set unit
        return a;
    }

    protected NodeDto dto(UiDoubleAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.doubleValue = uiAttribute.getValue();
        return dto;
    }

    public Value value(UiDoubleAttribute uiAttribute) {
        return new RealValue(uiAttribute.getValue(), null); // TODO: Pass unit - needs survey!
    }

    protected RealAttribute attribute(UiDoubleAttribute uiAttribute, NodeDefinition definition) {
        RealAttribute a = new RealAttribute((NumberAttributeDefinition) definition);
        a.setValue((RealValue) value(uiAttribute));
        return a;
    }
}
