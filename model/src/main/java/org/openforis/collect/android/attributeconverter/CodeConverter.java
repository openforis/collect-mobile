package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class CodeConverter extends AttributeConverter<CodeAttribute, UiCodeAttribute> {
    public UiCodeAttribute uiAttribute(Definition definition, CodeAttribute attribute) {
        UiCodeAttribute uiAttribute = new UiCodeAttribute(attribute.getId(), definition);
        Code attributeValue = attribute.getValue();
        String value = attributeValue.getCode();
        if (value != null) {

//            CodeList codeList = attribute.getDefinition().getList();
//            String label = codeList.getItem(value).getLabel();
            String label = null; // TODO: Need to get the label somehow
            // This will in practice only be called when created an enumerated property
            // In this case, couldn't we expect the item to be added to the NodeDefinition?
            uiAttribute.setCode(new UiCode(value, label));
        }
        return uiAttribute;
    }

    protected UiCodeAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiCodeAttribute uiAttribute = new UiCodeAttribute(nodeDto.id, definition);
        if (nodeDto.codeValue != null)
            uiAttribute.setCode(new UiCode(nodeDto.codeValue, nodeDto.codeLabel));
        return uiAttribute;
    }

    protected NodeDto dto(UiCodeAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        UiCode code = uiAttribute.getCode();
        if (code != null) {
            dto.codeValue = code.getValue();
            dto.codeLabel = code.getLabel();
        }
        return dto;

    }

    public Value value(UiCodeAttribute uiAttribute) {
        UiCode code = uiAttribute.getCode();
        return new Code(code == null ? null : code.getValue());
    }

    protected CodeAttribute attribute(UiCodeAttribute uiAttribute, NodeDefinition definition) {
        CodeAttribute a = new CodeAttribute((CodeAttributeDefinition) definition);
        a.setValue((Code) value(uiAttribute));
        return a;
    }
}
