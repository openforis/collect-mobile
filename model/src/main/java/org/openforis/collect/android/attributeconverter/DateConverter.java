package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiDateAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Value;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Daniel Wiell
 */
class DateConverter extends AttributeConverter<DateAttribute, UiDateAttribute> {
    public UiDateAttribute uiAttribute(Definition definition, DateAttribute attribute) {
        UiDateAttribute uiAttribute = new UiDateAttribute(attribute.getId(), definition);
        if (attribute.getValue() != null && attribute.getValue().toJavaDate() != null)
            uiAttribute.setDate(attribute.getValue().toJavaDate());
        return uiAttribute;
    }

    protected UiDateAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiDateAttribute uiAttribute = new UiDateAttribute(nodeDto.id, definition);
        uiAttribute.setDate(nodeDto.date);
        return uiAttribute;
    }

    protected NodeDto dto(UiDateAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.date = uiAttribute.getDate();
        return dto;
    }

    public Value value(UiDateAttribute uiAttribute) {
        java.util.Date date = uiAttribute.getDate();
        if (date == null)
            return new Date(null, null, null);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return new Date(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    protected DateAttribute attribute(UiDateAttribute uiAttribute, NodeDefinition definition) {
        DateAttribute a = new DateAttribute((DateAttributeDefinition) definition);
        a.setValue((Date) value(uiAttribute));
        return a;
    }
}
