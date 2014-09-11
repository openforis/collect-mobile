package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class TimeConverter extends AttributeConverter<TimeAttribute, UiTimeAttribute> {
    public UiTimeAttribute uiAttribute(Definition definition, TimeAttribute attribute) {
        UiTimeAttribute uiAttribute = new UiTimeAttribute(attribute.getId(), isRelevant(attribute), definition);
        if (attribute.getHour() != null && attribute.getMinute() != null)
            uiAttribute.setTime(attribute.getHour(), attribute.getMinute());
        return uiAttribute;
    }

    protected UiTimeAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiTimeAttribute uiAttribute = new UiTimeAttribute(nodeDto.id, nodeDto.relevant, definition);
        uiAttribute.setTime(nodeDto.hour, nodeDto.minute);
        return uiAttribute;
    }

    protected NodeDto dto(UiTimeAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.hour = uiAttribute.getHour();
        dto.minute = uiAttribute.getMinute();
        return dto;
    }

    public Value value(UiTimeAttribute uiAttribute) {
        return new Time(uiAttribute.getHour(), uiAttribute.getMinute());
    }

    protected TimeAttribute attribute(UiTimeAttribute uiAttribute, NodeDefinition definition) {
        TimeAttribute a = new TimeAttribute((TimeAttributeDefinition) definition);
        a.setValue((Time) value(uiAttribute));
        return a;
    }
}
