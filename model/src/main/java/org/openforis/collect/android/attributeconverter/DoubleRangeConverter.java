package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiDoubleRangeAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
// TODO: Set precision
public class DoubleRangeConverter extends AttributeConverter<RealRangeAttribute, UiDoubleRangeAttribute> {
    public UiDoubleRangeAttribute uiAttribute(Definition definition, RealRangeAttribute attribute) {
        UiDoubleRangeAttribute a = new UiDoubleRangeAttribute(attribute.getId(), isRelevant(attribute), definition);
        a.setFrom(attribute.getValue().getFrom()); // TODO: Set unit
        a.setTo(attribute.getValue().getTo());
        return a;
    }

    protected UiDoubleRangeAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiDoubleRangeAttribute a = new UiDoubleRangeAttribute(nodeDto.id, nodeDto.relevant, definition);
        a.setFrom(nodeDto.doubleFrom); // TODO: Set unit
        a.setTo(nodeDto.doubleTo);
        return a;
    }

    protected NodeDto dto(UiDoubleRangeAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.doubleFrom = uiAttribute.getFrom();
        dto.doubleTo = uiAttribute.getTo();
        return dto;
    }

    public Value value(UiDoubleRangeAttribute uiAttribute) {
        return new RealRange(uiAttribute.getFrom(), uiAttribute.getTo(), null); // TODO: Pass unit - needs survey!
    }

    protected RealRangeAttribute attribute(UiDoubleRangeAttribute uiAttribute, NodeDefinition definition) {
        RealRangeAttribute a = new RealRangeAttribute((RangeAttributeDefinition) definition);
        a.setValue((RealRange) value(uiAttribute));
        return a;
    }
}
