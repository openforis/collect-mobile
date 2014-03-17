package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiFileAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Value;

import java.io.File;

/**
 * @author Daniel Wiell
 */
class FileConverter extends AttributeConverter<FileAttribute, UiFileAttribute> {
    public UiFileAttribute uiAttribute(Definition definition, FileAttribute attribute) {
        UiFileAttribute uiAttribute = new UiFileAttribute(attribute.getId(), definition);
        uiAttribute.setFile(new File(attribute.getValue().getFilename()));
        return uiAttribute;
    }

    protected UiFileAttribute uiAttribute(NodeDto nodeDto, Definition definition) {
        UiFileAttribute uiAttribute = new UiFileAttribute(nodeDto.id, definition);
        uiAttribute.setFile(nodeDto.file);
        return uiAttribute;
    }

    protected NodeDto dto(UiFileAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        dto.file = uiAttribute.getFile();
        return dto;
    }

    public Value value(UiFileAttribute uiAttribute) {
        return new org.openforis.idm.model.File(uiAttribute.getFile().getAbsolutePath(), 0L); // TODO: File size
    }

    protected FileAttribute attribute(UiFileAttribute uiAttribute, NodeDefinition definition) {
        FileAttribute a = new FileAttribute((FileAttributeDefinition) definition);
        a.setValue((org.openforis.idm.model.File) value(uiAttribute));
        return a;
    }
}
