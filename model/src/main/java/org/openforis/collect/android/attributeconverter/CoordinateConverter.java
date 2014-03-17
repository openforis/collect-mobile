package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class CoordinateConverter extends AttributeConverter<CoordinateAttribute, UiCoordinateAttribute> {
    public UiCoordinateAttribute uiAttribute(Definition definition, CoordinateAttribute attribute) {
        UiCoordinateAttribute uiAttribute = new UiCoordinateAttribute(attribute.getId(), definition);
        Coordinate attributeValue = attribute.getValue();
        uiAttribute.setX(attributeValue.getX());
        uiAttribute.setY(attributeValue.getY());
        return uiAttribute;
    }

    protected UiCoordinateAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiCoordinateAttribute uiAttribute = new UiCoordinateAttribute(nodeDto.id, definition);
        uiAttribute.setX(nodeDto.x);
        uiAttribute.setY(nodeDto.y);
        return uiAttribute;
    }

    protected NodeDto dto(UiCoordinateAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.x = uiAttribute.getX();
        dto.y = uiAttribute.getY();
        return dto;
    }

    public Value value(UiCoordinateAttribute uiAttribute) {
        return new Coordinate(uiAttribute.getX(), uiAttribute.getY(), null); // TODO: srsId needed?
    }

    protected CoordinateAttribute attribute(UiCoordinateAttribute uiAttribute, NodeDefinition definition) {
        CoordinateAttribute a = new CoordinateAttribute((CoordinateAttributeDefinition) definition);
        a.setValue((Coordinate) value(uiAttribute));
        return a;
    }
}
