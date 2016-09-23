package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiCoordinateDefinition;
import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;
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
    public UiCoordinateAttribute uiAttribute(UiAttributeDefinition definition, CoordinateAttribute attribute) {
        UiCoordinateDefinition coordinateDefinition = (UiCoordinateDefinition) definition;
        UiCoordinateAttribute uiAttribute = new UiCoordinateAttribute(attribute.getId(), isRelevant(attribute), coordinateDefinition);

        updateUiAttributeValue(attribute, uiAttribute);
        return uiAttribute;
    }

    protected void updateUiAttributeValue(CoordinateAttribute attribute, UiCoordinateAttribute uiAttribute) {
        Coordinate attributeValue = attribute.getValue();
        uiAttribute.setX(attributeValue.getX());
        uiAttribute.setY(attributeValue.getY());
        String srsId = attribute.getSrsIdField().getValue();
        uiAttribute.setSpatialReferenceSystem(lookupSrs(uiAttribute.getDefinition(), srsId));
    }

    private UiSpatialReferenceSystem lookupSrs(UiCoordinateDefinition definition, String srsId) {
        return srsId == null ? null : definition.getById(srsId);
    }

    protected UiCoordinateAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiCoordinateDefinition coordinateDefinition = (UiCoordinateDefinition) definition;
        UiCoordinateAttribute uiAttribute = new UiCoordinateAttribute(nodeDto.id, nodeDto.relevant, coordinateDefinition);
        uiAttribute.setX(nodeDto.x);
        uiAttribute.setY(nodeDto.y);
        uiAttribute.setSpatialReferenceSystem(lookupSrs(coordinateDefinition, nodeDto.srs));
        return uiAttribute;
    }

    protected NodeDto dto(UiCoordinateAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.x = uiAttribute.getX();
        dto.y = uiAttribute.getY();
        UiSpatialReferenceSystem srs = uiAttribute.getSpatialReferenceSystem();
        dto.srs = srs == null ? null : srs.id;
        return dto;
    }

    public Value value(UiCoordinateAttribute uiAttribute) {
        UiSpatialReferenceSystem srs = uiAttribute.getSpatialReferenceSystem();
        String srsId = srs == null ? null : srs.id;
        Coordinate coordinate = new Coordinate(uiAttribute.getX(), uiAttribute.getY(), srsId);
        return coordinate.isComplete() ? coordinate : new Coordinate(null, null, null);
    }

    protected CoordinateAttribute attribute(UiCoordinateAttribute uiAttribute, NodeDefinition definition) {
        CoordinateAttribute a = new CoordinateAttribute((CoordinateAttributeDefinition) definition);
        if (!uiAttribute.isCalculated()) {
            Coordinate coordinate = (Coordinate) value(uiAttribute);
            a.setValue(coordinate);
        }
        return a;
    }
}
