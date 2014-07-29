package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.PreparedStatementHelper;
import org.openforis.idm.metamodel.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

// TODO: Rewrite this using DataSource
public class MobileCodeListItemDao extends org.openforis.collect.persistence.CodeListItemDao {
    // TODO: Proper caching...
    private final Map<String, List<PersistedCodeListItem>> childItemCache = new ConcurrentHashMap<String, List<PersistedCodeListItem>>();
    private final Map<String, PersistedCodeListItem> itemCache = new ConcurrentHashMap<String, PersistedCodeListItem>();
    private final AndroidDatabase database;

    public MobileCodeListItemDao(AndroidDatabase database) {
        super();
        this.database = database;
        setDataSource(database.dataSource());
    }

    @Override
    protected List<PersistedCodeListItem> loadChildItems(final CodeList codeList, Integer parentItemId, ModelVersion version) {
        String key = codeList.getId() + "|" + parentItemId;
        List<PersistedCodeListItem> result = childItemCache.get(key);
        if (result == null) {
            long startTime = System.currentTimeMillis();
            result = loadCodeListItems(codeList, parentItemId);
            Log.e("Mobile DAO", "Total time: " + (System.currentTimeMillis() - startTime) + " - listId: " + codeList.getId() + ", parentListId: " + parentItemId);
            childItemCache.put(key, result);
        } else {
            Log.e("Mobile DAO", "child items cache hit - listId: " + codeList.getId() + ", parentListId: " + parentItemId);
        }
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

    public List<PersistedCodeListItem> loadChildItems(final PersistedCodeListItem item, final ModelVersion version) {
        return time("loadChildItems", new Callable<List<PersistedCodeListItem>>() {
            public List<PersistedCodeListItem> call() throws Exception {
                return MobileCodeListItemDao.super.loadChildItems(item, version);
            }
        });
    }

    public PersistedCodeListItem loadItem(final CodeList codeList, final Integer parentItemId, final String code, final ModelVersion version) {
        return time("loadItem", new Callable<PersistedCodeListItem>() {
            public PersistedCodeListItem call() throws Exception {
                String key = codeList.getId() + "|" + parentItemId + "|" + code;
                PersistedCodeListItem item = itemCache.get(key);
                if (item == null) {
                    item = MobileCodeListItemDao.super.loadItem(codeList, parentItemId, code, version);
                    if (item != null) // TODO: Ugly!
                        itemCache.put(key, item);
                } else {
                    Log.e("Mobile DAO", "item cache hit - listId: " + codeList.getId() + ", parentItemId: " + parentItemId + ", code: " + code);
                }
                return item;
            }
        });
    }

    public PersistedCodeListItem loadItem(final CodeList codeList, final String code, final ModelVersion version) {
        return time("loadItem", new Callable<PersistedCodeListItem>() {
            public PersistedCodeListItem call() throws Exception {
                String key = codeList.getId() + "|" + null + "|" + code;
                PersistedCodeListItem item = itemCache.get(key);
                if (item == null) {
                    item = MobileCodeListItemDao.super.loadItem(codeList, code, version);
                    itemCache.put(key, item);
                } else {
                    Log.e("Mobile DAO", "item cache hit - listId: " + codeList.getId() + ", code: " + code);
                }
                return item;
            }
        });
    }

    /**
     * Inserts the items in batch.
     *
     * @param items
     */
    public void insert(final List<PersistedCodeListItem> items) {
        time("insert", new Runnable() {
            public void run() {
                database.execute(new ConnectionCallback<Void>() {
                    public Void execute(Connection connection) throws SQLException {
                        PersistedCodeListItem firstItem = items.get(0);
                        int surveyId = firstItem.getSurvey().getId();
                        int nextSystemId = maxSystemId(connection) + 1;
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO ofc_code_list(\n" +
                                "id, survey_id, code_list_id, item_id, parent_id, sort_order, code, qualifiable, since_version_id, " +
                                "deprecated_version_id, label1, label2, label3, description1, description2, description3)" +
                                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        for (PersistedCodeListItem item : items) {
                            if (item.getSystemId() == null)
                                item.setSystemId(nextSystemId);
                            nextSystemId = Math.max(item.getSystemId(), nextSystemId) + 1;
                            PreparedStatementHelper psh = new PreparedStatementHelper(ps);
                            psh.setInt(item.getSystemId());
                            psh.setInt(surveyId);
                            psh.setInt(item.getCodeList().getId());
                            psh.setInt(item.getId());
                            psh.setIntOrNull(item.getParentId());
                            psh.setIntOrNull(item.getSortOrder());
                            psh.setString(item.getCode());
                            psh.setBoolean(item.isQualifiable());
                            psh.setStringOrNull(item.getSinceVersionName());
                            psh.setStringOrNull(item.getDeprecatedVersionName());
                            for (String label : labels(item))
                                psh.setStringOrNull(label);
                            for (String description : descriptions(item))
                                psh.setStringOrNull(description);
                            ps.addBatch();
                        }

                        ps.executeBatch();
                        return null;
                    }
                });
            }
        });
    }

    private String[] labels(PersistedCodeListItem item) {
        int size = 3; // Three labels are persisted
        String[] result = new String[size];
        Survey survey = item.getSurvey();
        List<String> languages = survey.getLanguages();
        for (int i = 0; i < size; i++) {
            String label;
            if (i < languages.size()) {
                String lang = languages.get(i);
                label = item.getLabel(lang);
            } else {
                label = null;
            }
            result[i] = label;
        }
        return result;
    }

    private String[] descriptions(PersistedCodeListItem item) {
        int size = 3; // Three descriptions are persisted
        String[] result = new String[size];
        Survey survey = item.getSurvey();
        List<String> languages = survey.getLanguages();
        for (int i = 0; i < size; i++) {
            String description;
            if (i < languages.size()) {
                String lang = languages.get(i);
                description = item.getDescription(lang);
            } else {
                description = null;
            }
            result[i] = description;
        }
        return result;
    }

    private int maxSystemId(Connection connection) throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT MAX(id) id FROM ofc_code_list");
        rs.next();
        int maxId = rs.getInt("id");
        rs.close();
        return maxId;
    }

    /*
            Integer id = (Integer) select(max(idField)).from(idField.getTable()).fetchOne(0);
			if ( id == null ) {
				return 1;
			} else {
				return id + 1;
			}
			*/


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
        String qualifiable = cursor.getString(cursor.getColumnIndex(OFC_CODE_LIST.QUALIFIABLE.getName()));
        entity.setQualifiable(!"0".equals(qualifiable));
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


    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(MobileCodeListItemDao.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(MobileCodeListItemDao.class, methodName, action);
    }
}
