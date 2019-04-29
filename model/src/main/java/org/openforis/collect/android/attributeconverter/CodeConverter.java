package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeAttributeDefinition;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class CodeConverter extends AttributeConverter<CodeAttribute, UiCodeAttribute> {
    public UiCodeAttribute uiAttribute(UiAttributeDefinition definition, CodeAttribute attribute) {
        UiCodeAttribute uiAttribute = new UiCodeAttribute(attribute.getId(), isRelevant(attribute),
                (UiCodeAttributeDefinition) definition);
        updateUiAttributeValue(attribute, uiAttribute);
        return uiAttribute;
    }

    protected void updateUiAttributeValue(CodeAttribute attribute, UiCodeAttribute uiAttribute) {
        Code attributeValue = attribute.getValue();
        String value = attributeValue.getCode();
        if (value != null) {
            String label = null; // TODO: Need to get the label somehow
            uiAttribute.setCode(new UiCode(value, label, null, uiAttribute.getDefinition().isValueShown()));
        } else {
            uiAttribute.setCode(null);
        }
    }

    protected UiCodeAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiCodeAttributeDefinition codeAttributeDefinition = (UiCodeAttributeDefinition) definition;
        UiCodeAttribute uiAttribute = new UiCodeAttribute(nodeDto.id, nodeDto.relevant,
                codeAttributeDefinition);
        if (nodeDto.codeValue != null)
            uiAttribute.setCode(new UiCode(nodeDto.codeValue, nodeDto.codeLabel, null, codeAttributeDefinition.isValueShown()));
        uiAttribute.setQualifier(nodeDto.codeQualifier);
        return uiAttribute;
    }

    protected NodeDto dto(UiCodeAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        UiCode code = uiAttribute.getCode();
        if (code != null) {
            dto.codeValue = code.getValue();
            dto.codeLabel = code.getLabel();
        }
        dto.codeQualifier = uiAttribute.getQualifier();
        return dto;
    }

    public Value value(UiCodeAttribute uiAttribute) {
        UiCode code = uiAttribute.getCode();
        return new Code(code == null ? null : code.getValue(), uiAttribute.getQualifier());
    }

    protected CodeAttribute attribute(UiCodeAttribute uiAttribute, NodeDefinition definition) {
        CodeAttribute a = new CodeAttribute((CodeAttributeDefinition) definition);
        if (!uiAttribute.isCalculated() || uiAttribute.isCalculatedOnlyOneTime())
            a.setValue((Code) value(uiAttribute));
        return a;
    }
}
