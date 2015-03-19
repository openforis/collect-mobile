package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.*;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
@SuppressWarnings("unchecked")
public abstract class AttributeConverter<T extends Attribute, U extends UiAttribute> {

    protected abstract U uiAttribute(UiAttributeDefinition definition, T attribute);

    protected abstract void updateUiAttributeValue(T attribute, U uiAttribute);

    protected abstract U uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition);

    protected abstract NodeDto dto(U uiAttribute);

    protected abstract Value value(U uiAttribute);

    protected abstract T attribute(U uiAttribute, NodeDefinition definition);

    protected final NodeDto createDto(UiAttribute uiAttribute) {
        NodeDto dto = new NodeDto();
        dto.id = uiAttribute.getId();
        dto.relevant = uiAttribute.isRelevant();
        dto.status = uiAttribute.getStatus().name();
        dto.definitionId = uiAttribute.getDefinition().id;
        dto.parentId = uiAttribute.getParent() == null ? null : uiAttribute.getParent().getId();
        dto.surveyId = uiAttribute.getUiSurvey().getId();
        dto.recordId = uiAttribute.getUiRecord().getId();
        dto.type = NodeDto.Type.ofUiNode(uiAttribute);
        dto.recordKeyAttribute = uiAttribute.getUiRecord().isKeyAttribute(uiAttribute);
        return dto;
    }

    protected boolean isRelevant(Attribute attribute) {
        return attribute.getParent().isRelevant(attribute.getName());
    }

    public static <D extends UiAttributeDefinition> UiAttribute toUiAttribute(D definition, Attribute attribute) {
        UiAttribute uiAttribute = getConverter(attribute).uiAttribute(definition, attribute);
        uiAttribute.setRelevant(attribute.getParent().isRelevant(attribute.getName()));
        return uiAttribute;
    }

    public static UiAttribute toUiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        return getConverter(nodeDto.type.uiNodeClass).uiAttribute(nodeDto, definition);
    }

    public static NodeDto toDto(UiAttribute attribute) {
        return getConverter(attribute.getClass()).dto(attribute);
    }

    public static Value toValue(UiAttribute uiAttribute) {
        return getConverter(uiAttribute.getClass()).value(uiAttribute);
    }

    public static Attribute toAttribute(UiAttribute uiAttribute, NodeDefinition definition) {
        Attribute attribute = getConverter(definition).attribute(uiAttribute, definition);
        attribute.setId(uiAttribute.getId());
        attribute.updateSummaryInfo();
        return attribute;
    }

    public static <T extends Attribute, U extends UiAttribute> void updateUiValue(T attribute, U uiAttribute) {
        getConverter(attribute).updateUiAttributeValue(attribute, uiAttribute);
    }

    private static AttributeConverter getConverter(Attribute attribute) {
        return getConverter(attribute.getDefinition());
    }

    private static AttributeConverter getConverter(NodeDefinition definition) {
        if (definition instanceof TextAttributeDefinition)
            return new TextConverter();
        if (definition instanceof DateAttributeDefinition)
            return new DateConverter();
        if (definition instanceof TimeAttributeDefinition)
            return new TimeConverter();
        if (definition instanceof CodeAttributeDefinition)
            return new CodeConverter();
        if (definition instanceof CoordinateAttributeDefinition)
            return new CoordinateConverter();
        if (definition instanceof FileAttributeDefinition)
            return new FileConverter();
        if (definition instanceof TaxonAttributeDefinition)
            return new TaxonConverter();
        if (definition instanceof BooleanAttributeDefinition)
            return new BooleanConverter();
        if (definition instanceof NumberAttributeDefinition && ((NumberAttributeDefinition) definition).isInteger())
            return new IntegerConverter();
        if (definition instanceof NumberAttributeDefinition && ((NumberAttributeDefinition) definition).isReal())
            return new DoubleConverter();
        if (definition instanceof RangeAttributeDefinition && ((RangeAttributeDefinition) definition).isInteger())
            return new IntegerRangeConverter();
        if (definition instanceof RangeAttributeDefinition && ((RangeAttributeDefinition) definition).isReal())
            return new DoubleRangeConverter();
        throw new IllegalStateException("Unexpected attribute type: " + definition);
    }


    public static Class<? extends UiAttribute> getUiAttributeType(NodeDefinition definition) {
        if (definition instanceof TextAttributeDefinition)
            return UiTextAttribute.class;
        if (definition instanceof DateAttributeDefinition)
            return UiDateAttribute.class;
        if (definition instanceof TimeAttributeDefinition)
            return UiTaxonAttribute.class;
        if (definition instanceof CodeAttributeDefinition)
            return UiCodeAttribute.class;
        if (definition instanceof CoordinateAttributeDefinition)
            return UiCodeAttribute.class;
        if (definition instanceof FileAttributeDefinition)
            return UiFileAttribute.class;
        if (definition instanceof TaxonAttributeDefinition)
            return UiTaxonAttribute.class;
        if (definition instanceof BooleanAttributeDefinition)
            return UiBooleanAttribute.class;
        if (definition instanceof NumberAttributeDefinition && ((NumberAttributeDefinition) definition).isInteger())
            return UiIntegerAttribute.class;
        if (definition instanceof NumberAttributeDefinition && ((NumberAttributeDefinition) definition).isReal())
            return UiDoubleAttribute.class;
        if (definition instanceof RangeAttributeDefinition && ((RangeAttributeDefinition) definition).isInteger())
            return UiIntegerRangeAttribute.class;
        if (definition instanceof RangeAttributeDefinition && ((RangeAttributeDefinition) definition).isReal())
            return UiDoubleRangeAttribute.class;
        throw new IllegalStateException("Unexpected attribute type: " + definition);
    }

    private static AttributeConverter getConverter(Class type) {
        if (UiTextAttribute.class.isAssignableFrom(type))
            return new TextConverter();
        if (UiDateAttribute.class.isAssignableFrom(type))
            return new DateConverter();
        if (UiTimeAttribute.class.isAssignableFrom(type))
            return new TimeConverter();
        if (UiCodeAttribute.class.isAssignableFrom(type))
            return new CodeConverter();
        if (UiCoordinateAttribute.class.isAssignableFrom(type))
            return new CoordinateConverter();
        if (UiFileAttribute.class.isAssignableFrom(type))
            return new FileConverter();
        if (UiTaxonAttribute.class.isAssignableFrom(type))
            return new TaxonConverter();
        if (UiBooleanAttribute.class.isAssignableFrom(type))
            return new BooleanConverter();
        if (UiIntegerAttribute.class.isAssignableFrom(type))
            return new IntegerConverter();
        if (UiDoubleAttribute.class.isAssignableFrom(type))
            return new DoubleConverter();
        if (UiIntegerRangeAttribute.class.isAssignableFrom(type))
            return new IntegerRangeConverter();
        if (UiDoubleRangeAttribute.class.isAssignableFrom(type))
            return new DoubleRangeConverter();
        throw new IllegalStateException("Unexpected UiAttribute type: " + type);
    }
}
