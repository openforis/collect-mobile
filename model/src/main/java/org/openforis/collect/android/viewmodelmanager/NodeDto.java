package org.openforis.collect.android.viewmodelmanager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.idm.model.Coordinate;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Locale.ENGLISH;

/**
 * @author Daniel Wiell
 */
public class NodeDto {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", ENGLISH);

    public int id;
    public boolean relevant;
    public String status;
    public Integer parentId;
    public Integer parentEntityId;
    public String definitionId;
    public int surveyId;
    public String recordCollectionName;
    public int recordId;
    public boolean recordKeyAttribute;
    public Type type;
    public String text;
    public Date date;
    public Integer hour;
    public Integer minute;
    public String codeValue;
    public String codeQualifier;
    public String codeLabel;
    public Boolean booleanValue;
    public Integer intValue;
    public Integer intFrom;
    public Integer intTo;
    public Double doubleValue;
    public Double doubleFrom;
    public Double doubleTo;
    public Double x;
    public Double y;
    public String srs;
    public Double altitude;
    public Double accuracy;
    public String taxonCode;
    public String taxonScientificName;
    public File file;
    public Date createdOn = new Date();
    public Date modifiedOn = new Date();

    public static NodeDto recordKeyAttribute(int recordId, String definitionId, String value, Class<? extends UiAttribute> attributeType) {
        NodeDto node = new NodeDto();
        node.recordId = recordId;
        node.type = NodeDto.Type.ofUiNodeType(attributeType);
        node.definitionId = definitionId;
        if (value != null && !value.isEmpty()) {
            switch (node.type) {
                case CODE_ATTRIBUTE:
                    node.codeValue = value;
                    break;
                case COORDINATE_ATTRIBUTE:
                    Coordinate coordinate = Coordinate.parseCoordinate(value);
                    node.x = coordinate == null ? null : coordinate.getX();
                    node.y = coordinate == null ? null : coordinate.getY();
                    node.srs = coordinate == null ? null : coordinate.getSrsId();
                    break;
                case DOUBLE_ATTRIBUTE:
                    node.doubleValue = Double.parseDouble(value);
                    break;
                case INTEGER_ATTRIBUTE:
                    node.intValue = Integer.parseInt(value);
                    break;
                case TEXT_ATTRIBUTE:
                    node.text = value;
                    break;
                case DATE_ATTRIBUTE:
                    try {
                        node.date = DATE_FORMATTER.parse(value);
                    } catch (ParseException e) {
                        throw new IllegalStateException("Unexpected date format: " + value);
                    }
                    break;
                case TIME_ATTRIBUTE:
                    node.hour = Integer.parseInt(value.substring(0, 2));
                    node.minute = Integer.parseInt(value.substring(3, 5));
                    break;
                default:
                    throw new IllegalStateException("Attribute type cannot be record key: " + attributeType);
            }
        }
        return node;
    }

    public String toString() {
        return id + ": " + type;
    }

    public enum Type {
        RECORD(1, UiRecord.class),
        ENTITY(2, UiEntity.class),
        INTERNAL_NODE(3, UiInternalNode.class),
        ENTITY_COLLECTION(4, UiEntityCollection.class),
        ATTRIBUTE_COLLECTION(5, UiAttributeCollection.class),
        BOOLEAN_ATTRIBUTE(6, UiBooleanAttribute.class),
        CODE_ATTRIBUTE(7, UiCodeAttribute.class),
        COORDINATE_ATTRIBUTE(8, UiCoordinateAttribute.class),
        DATE_ATTRIBUTE(9, UiDateAttribute.class),
        DOUBLE_ATTRIBUTE(10, UiDoubleAttribute.class),
        DOUBLE_RANGE_ATTRIBUTE(11, UiDoubleRangeAttribute.class),
        FILE_ATTRIBUTE(12, UiFileAttribute.class),
        INTEGER_ATTRIBUTE(13, UiIntegerAttribute.class),
        INTEGER_RANGE_ATTRIBUTE(14, UiIntegerRangeAttribute.class),
        TAXON_ATTRIBUTE(15, UiTaxonAttribute.class),
        TEXT_ATTRIBUTE(16, UiTextAttribute.class),
        TIME_ATTRIBUTE(17, UiTimeAttribute.class);

        private static final Map<Integer, Type> TYPE_BY_ID = new HashMap<Integer, Type>();
        private static final Map<Class<? extends UiNode>, Type> TYPE_BY_CLASS = new HashMap<Class<? extends UiNode>, Type>();

        public final int id;
        public final Class<? extends UiNode> uiNodeClass;

        Type(int id, Class<? extends UiNode> uiNodeClass) {
            this.id = id;
            this.uiNodeClass = uiNodeClass;
        }

        public static Type byId(int id) {
            Type type = TYPE_BY_ID.get(id);
            if (type == null) {
                for (Type t : values())
                    if (t.id == id) {
                        TYPE_BY_ID.put(id, t);
                        return t;
                    }
                throw new IllegalArgumentException("No type with id " + id);
            }
            return type;
        }

        public static Type ofUiNode(UiNode node) {
            Class<? extends UiNode> uiNodeClass = node.getClass();
            return ofUiNodeType(uiNodeClass);
        }

        public static Type ofUiNodeType(Class<? extends UiNode> uiNodeType) {
            Type type = TYPE_BY_CLASS.get(uiNodeType);
            if (type == null) {
                for (Type t : values()) {
                    if (t.uiNodeClass == uiNodeType) {
                        TYPE_BY_CLASS.put(uiNodeType, t);
                        return t;
                    }
                }
                throw new IllegalArgumentException("No type for class " + uiNodeType);
            }
            return type;
        }
    }

    public static class Collection {
        private final Map<Integer, List<NodeDto>> nodesByParentId = new HashMap<Integer, List<NodeDto>>();

        public NodeDto getRootNode() {
            List<NodeDto> roots = nodesByParentId.get(null);
            if (roots == null || roots.size() != 1)
                throw new IllegalStateException("Expected exactly one root node. Got " + roots);
            return roots.get(0);
        }

        public List<NodeDto> childrenOf(Integer parentId) {
            List<NodeDto> nodes = nodesByParentId.get(parentId);
            return nodes == null ? new ArrayList<NodeDto>() : nodes;
        }

        public void addNode(NodeDto nodeDto) {
            List<NodeDto> siblings = nodesByParentId.get(nodeDto.parentId);
            if (siblings == null) {
                siblings = new ArrayList<NodeDto>();
                nodesByParentId.put(nodeDto.parentId, siblings);
            }
            siblings.add(nodeDto);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NodeDto nodeDto = (NodeDto) o;

        return new EqualsBuilder()
                .append(id, nodeDto.id)
                .append(relevant, nodeDto.relevant)
                .append(surveyId, nodeDto.surveyId)
                .append(recordId, nodeDto.recordId)
                .append(recordKeyAttribute, nodeDto.recordKeyAttribute)
                .append(status, nodeDto.status)
                .append(parentId, nodeDto.parentId)
                .append(parentEntityId, nodeDto.parentEntityId)
                .append(definitionId, nodeDto.definitionId)
                .append(recordCollectionName, nodeDto.recordCollectionName)
                .append(type, nodeDto.type)
                .append(text, nodeDto.text)
                .append(date, nodeDto.date)
                .append(hour, nodeDto.hour).append(minute, nodeDto.minute)
                .append(codeValue, nodeDto.codeValue).append(codeQualifier, nodeDto.codeQualifier).append(codeLabel, nodeDto.codeLabel)
                .append(booleanValue, nodeDto.booleanValue)
                .append(intValue, nodeDto.intValue)
                .append(intFrom, nodeDto.intFrom).append(intTo, nodeDto.intTo)
                .append(doubleValue, nodeDto.doubleValue)
                .append(doubleFrom, nodeDto.doubleFrom).append(doubleTo, nodeDto.doubleTo)
                .append(x, nodeDto.x).append(y, nodeDto.y).append(srs, nodeDto.srs).append(altitude, nodeDto.altitude).append(accuracy, nodeDto.accuracy)
                .append(taxonCode, nodeDto.taxonCode).append(taxonScientificName, nodeDto.taxonScientificName)
//                .append(file, nodeDto.file)
//                .append(createdOn, nodeDto.createdOn)
//                .append(modifiedOn, nodeDto.modifiedOn)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id).append(relevant).append(status).append(parentId).append(parentEntityId).append(definitionId).append(surveyId)
                .append(recordCollectionName).append(recordId).append(recordKeyAttribute).append(type)
                .append(text)
                .append(date)
                .append(hour).append(minute)
                .append(codeValue).append(codeQualifier).append(codeLabel)
                .append(booleanValue)
                .append(intValue)
                .append(intFrom).append(intTo)
                .append(doubleValue)
                .append(doubleFrom).append(doubleTo)
                .append(x).append(y).append(srs).append(altitude).append(accuracy)
                .append(taxonCode).append(taxonScientificName)
//                .append(file)
//                .append(createdOn)
//                .append(modifiedOn)
                .toHashCode();
    }
}
