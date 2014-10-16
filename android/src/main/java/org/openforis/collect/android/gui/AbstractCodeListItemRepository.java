package org.openforis.collect.android.gui;

import android.database.Cursor;
import org.openforis.idm.metamodel.*;

import java.util.List;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

public abstract class AbstractCodeListItemRepository {
    protected final PersistedCodeListItem createCodeListItem(Cursor cursor, CodeList codeList) {
        PersistedCodeListItem item;
        int itemId = cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.ITEM_ID.getName()));
        item = new PersistedCodeListItem(codeList, itemId);
        item.setSystemId(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.ID.getName())));
        item.setSortOrder(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.SORT_ORDER.getName())));
        item.setCode(cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.CODE.getName())));
        item.setParentId(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.PARENT_ID.getName())));
        String qualifiable = cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.QUALIFIABLE.getName()));
        item.setQualifiable(!"0".equals(qualifiable));
        item.setSinceVersion(extractModelVersion(item, cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.SINCE_VERSION_ID.getName()))));
        item.setDeprecatedVersion(extractModelVersion(item, cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.DEPRECATED_VERSION_ID.getName()))));
        extractLabels(codeList, cursor, item);
        extractDescriptions(codeList, cursor, item);
        return item;
    }

    private ModelVersion extractModelVersion(SurveyObject surveyObject, Integer versionId) {
        Survey survey = surveyObject.getSurvey();
        return ((versionId == null) || (versionId == 0)) ? null : survey.getVersionById(versionId);
    }

    private void extractLabels(CodeList codeList, Cursor crs, PersistedCodeListItem item) {
        Survey survey = codeList.getSurvey();
        item.removeAllLabels();
        List<String> languages = survey.getLanguages();
        String[] labelColumnNames = {OFC_CODE_LIST.LABEL1.getName(), OFC_CODE_LIST.LABEL2.getName(), OFC_CODE_LIST.LABEL3.getName()};
        for (int i = 0; i < languages.size(); i++) {
            String lang = languages.get(i);
            String label = crs.getString(crs.getColumnIndex(labelColumnNames[i]));
            item.setLabel(lang, label);
            if (i >= 3)
                break;
        }
    }

    private void extractDescriptions(CodeList codeList, Cursor crs, PersistedCodeListItem item) {
        Survey survey = codeList.getSurvey();
        item.removeAllDescriptions();
        List<String> languages = survey.getLanguages();
        String[] descrColumnNames = {OFC_CODE_LIST.DESCRIPTION1.getName(), OFC_CODE_LIST.DESCRIPTION2.getName(), OFC_CODE_LIST.DESCRIPTION3.getName()};
        for (int i = 0; i < languages.size(); i++) {
            String lang = languages.get(i);
            String label = crs.getString(crs.getColumnIndex(descrColumnNames[i]));
            item.setDescription(lang, label);
            if (i >= 3)
                break;
        }
    }

    protected final String constraint(Number value) {
        return value == null ? " is null" : " = " + value;
    }

    protected final String parameterizedConstraint(String value) {
        return value == null ? " is null" : " = ?";
    }

}
