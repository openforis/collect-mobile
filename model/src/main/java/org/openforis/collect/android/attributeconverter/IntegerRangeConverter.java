package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiIntegerRangeAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
// TODO: Set precision
public class IntegerRangeConverter extends AttributeConverter<IntegerRangeAttribute, UiIntegerRangeAttribute> {
    public UiIntegerRangeAttribute uiAttribute(Definition definition, IntegerRangeAttribute attribute) {
        UiIntegerRangeAttribute a = new UiIntegerRangeAttribute(attribute.getId(), isRelevant(attribute), definition);
        a.setFrom(attribute.getValue().getFrom()); // TODO: Set unit
        a.setTo(attribute.getValue().getTo());
        return a;
    }

    protected UiIntegerRangeAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiIntegerRangeAttribute a = new UiIntegerRangeAttribute(nodeDto.id, nodeDto.relevant, definition);
        a.setFrom(nodeDto.intFrom); // TODO: Set unit
        a.setTo(nodeDto.intTo);
        return a;
    }

    protected NodeDto dto(UiIntegerRangeAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.intFrom = uiAttribute.getFrom();
        dto.intTo = uiAttribute.getTo();
        return dto;
    }

    public Value value(UiIntegerRangeAttribute uiAttribute) {
        return new IntegerRange(uiAttribute.getFrom(), uiAttribute.getTo(), null); // TODO: Pass unit - needs survey!
    }

    protected IntegerRangeAttribute attribute(UiIntegerRangeAttribute uiAttribute, NodeDefinition definition) {
        IntegerRangeAttribute a = new IntegerRangeAttribute((RangeAttributeDefinition) definition);
        a.setValue((IntegerRange) value(uiAttribute));
        return a;
    }
}
