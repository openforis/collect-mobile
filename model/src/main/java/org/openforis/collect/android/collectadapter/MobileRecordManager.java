package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class MobileRecordManager extends RecordManager {
    private final RecordUniquenessChecker uniquenessChecker;

    public MobileRecordManager(CodeListManager codeListManager, SurveyManager surveyManager, RecordUniquenessChecker uniquenessChecker) {
        super(false);
        this.uniquenessChecker = uniquenessChecker;
        setCodeListManager(codeListManager);
        setSurveyManager(surveyManager);
    }

    public boolean isUnique(CollectRecord record) {
        record.updateRootEntityKeyValues();
        List<String> keyValues = record.getRootEntityKeyValues();
        List<NodeDto> keys = new ArrayList<NodeDto>();
        EntityDefinition rootDefinition = record.getRootEntity().getDefinition();
        List<AttributeDefinition> keyDefinitions = rootDefinition.getKeyAttributeDefinitions();
        for (int i = 0; i < keyDefinitions.size(); i++) {
            AttributeDefinition keyDefinition = keyDefinitions.get(i);
            Class<? extends UiAttribute> keyAttributeType = AttributeConverter.getUiAttributeType(keyDefinition);
            String value = keyValues.get(i);
            NodeDto keyDto = NodeDto.recordKeyAttribute(record.getRootEntity().getId(), String.valueOf(keyDefinition.getId()), value, keyAttributeType);
            keys.add(keyDto);
        }
        return uniquenessChecker.isUnique(keys);
    }
}
