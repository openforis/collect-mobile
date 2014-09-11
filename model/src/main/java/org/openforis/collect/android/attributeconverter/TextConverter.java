package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiTextAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class TextConverter extends AttributeConverter<TextAttribute, UiTextAttribute> {
    public UiTextAttribute uiAttribute(Definition definition, TextAttribute attribute) {
        UiTextAttribute uiAttribute = new UiTextAttribute(attribute.getId(), isRelevant(attribute), definition);
        uiAttribute.setText(attribute.getText());
        return uiAttribute;
    }

    protected UiTextAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiTextAttribute uiAttribute = new UiTextAttribute(nodeDto.id, nodeDto.relevant, definition);
        uiAttribute.setText(nodeDto.text);
        return uiAttribute;
    }

    protected NodeDto dto(UiTextAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.text = uiAttribute.getText();
        return dto;
    }

    public Value value(UiTextAttribute uiAttribute) {
        return new TextValue(uiAttribute.getText());
    }

    protected TextAttribute attribute(UiTextAttribute uiAttribute, NodeDefinition definition) {
        TextAttribute a = new TextAttribute((TextAttributeDefinition) definition);
        a.setValue((TextValue) value(uiAttribute));
        return a;
    }
}
