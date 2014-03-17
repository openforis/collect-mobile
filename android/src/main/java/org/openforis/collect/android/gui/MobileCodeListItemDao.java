package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.idm.metamodel.*;

import java.util.ArrayList;
import java.util.List;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

// TODO: Rewrite this using DataSource
public class MobileCodeListItemDao extends org.openforis.collect.persistence.CodeListItemDao {
    private final AndroidDatabase database;

    public MobileCodeListItemDao(AndroidDatabase database) {
        super();
        this.database = database;
        setDataSource(database.dataSource());
    }

    @Override
    protected List<PersistedCodeListItem> loadChildItems(final CodeList codeList, Integer parentItemId, ModelVersion version) {
        long startTime = System.currentTimeMillis();
        List<PersistedCodeListItem> result = loadCodeListItems(codeList, parentItemId);
        Log.e("Mobile DAO", "Total time: " + (System.currentTimeMillis() - startTime));
        return result;
    }

    private List<PersistedCodeListItem> loadCodeListItems(final CodeList codeList, final Integer parentItemId) {
        return database.execute(new AndroidDatabaseCallback<List<PersistedCodeListItem>>() {
            public List<PersistedCodeListItem> execute(SQLiteDatabase database) {
                Cursor cursor = database.rawQuery("" +
                        "select * from " + OFC_CODE_LIST
                        + " where " + OFC_CODE_LIST.CODE_LIST_ID + constraint(codeList.getId())
                        + " and " + OFC_CODE_LIST.PARENT_ID + constraint(parentItemId)
                        + " order by " + OFC_CODE_LIST.SORT_ORDER, null);
                try {
                PersistedCodeListItem entity;
                List<PersistedCodeListItem> result = new ArrayList<PersistedCodeListItem>();
                if (cursor.moveToFirst())
                    do {
                        entity = createCodeListItem(cursor, codeList);
                        result.add(entity);
                    } while (cursor.moveToNext());
                return result;
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        });
    }

    private String constraint(Object value) {
        return value == null ? " is null" : " = " + value;
    }

    private PersistedCodeListItem createCodeListItem(Cursor cursor, CodeList codeList) {
        PersistedCodeListItem entity;
        int itemId = cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.ITEM_ID.getName()));
        entity = new PersistedCodeListItem(codeList, itemId);
        entity.setSystemId(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.ID.getName())));
        entity.setSortOrder(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.SORT_ORDER.getName())));
        entity.setCode(cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.CODE.getName())));
        entity.setParentId(cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.PARENT_ID.getName())));
        entity.setQualifiable(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.QUALIFIABLE.getName()))));
        entity.setSinceVersion(extractModelVersion(entity, cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.SINCE_VERSION_ID.getName()))));
        entity.setDeprecatedVersion(extractModelVersion(entity, cursor.getInt(cursor.getColumnIndex(OFC_CODE_LIST.DEPRECATED_VERSION_ID.getName()))));
        extractLabels(codeList, cursor, entity);
        extractDescriptions(codeList, cursor, entity);
        return entity;
    }


    protected ModelVersion extractModelVersion(SurveyObject surveyObject, Integer versionId) {
        Survey survey = surveyObject.getSurvey();
        return ((versionId == null) || (versionId == 0)) ? null : survey.getVersionById(versionId);
    }

    protected void extractLabels(CodeList codeList, Cursor crs, PersistedCodeListItem item) {
        Survey survey = codeList.getSurvey();
        item.removeAllLabels();
        List<String> languages = survey.getLanguages();
//		Log.e("Mobile DAO", "Set labels. Number of languages is: " + languages.size() + " SurveyId is " + surveyService.getId());
        String[] labelColumnNames = {OFC_CODE_LIST.LABEL1.getName(), OFC_CODE_LIST.LABEL2.getName(), OFC_CODE_LIST.LABEL3.getName()};
        for (int i = 0; i < languages.size(); i++) {
            String lang = languages.get(i);
            String label = crs.getString(crs.getColumnIndex(labelColumnNames[i]));
//			Log.e("Mobile DAO", "Set label: " + label + " for language: " + lang);
            item.setLabel(lang, label);
            if (i >= 3)
                break;
        }
    }

    protected void extractDescriptions(CodeList codeList, Cursor crs, PersistedCodeListItem item) {
        Survey survey = codeList.getSurvey();
        item.removeAllDescriptions();
        List<String> languages = survey.getLanguages();
//		Log.e("Mobile DAO", "Set description. Number of languages is: " + languages.size() + " SurveyId is " + surveyService.getId());
        String[] descrColumnNames = {OFC_CODE_LIST.DESCRIPTION1.getName(), OFC_CODE_LIST.DESCRIPTION2.getName(), OFC_CODE_LIST.DESCRIPTION3.getName()};
        for (int i = 0; i < languages.size(); i++) {
            String lang = languages.get(i);
            String label = crs.getString(crs.getColumnIndex(descrColumnNames[i]));
//			Log.e("Mobile DAO", "Set description: " + label + " for language: " + lang);
            item.setDescription(lang, label);
            if (i >= 3)
                break;
        }
    }

}
