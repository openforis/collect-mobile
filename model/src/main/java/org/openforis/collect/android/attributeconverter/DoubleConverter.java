package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
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
    public UiDoubleAttribute uiAttribute(UiAttributeDefinition definition, RealAttribute attribute) {
        UiDoubleAttribute a = new UiDoubleAttribute(attribute.getId(), isRelevant(attribute), definition);
        updateUiAttributeValue(attribute, a);
        return a;
    }

    protected void updateUiAttributeValue(RealAttribute attribute, UiDoubleAttribute uiAttribute) {
        uiAttribute.setValue(attribute.getValue().getValue()); // TODO: Set unit
    }

    protected UiDoubleAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
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
        if (!uiAttribute.isCalculated())
            a.setValue((RealValue) value(uiAttribute));
        return a;
    }
}
