package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.idm.metamodel.*;

import java.util.ArrayList;
import java.util.List;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

public class ChildCodeListItemsRepository extends AbstractCodeListItemRepository {
    public static final int ONE_MILLION_ITEMS = 1000 * 1000;
    private final LoadingCache<Key, List<PersistedCodeListItem>> cache;
    private final AndroidDatabase database;

    public ChildCodeListItemsRepository(AndroidDatabase database) {
        this.database = database;
        CacheLoader<Key, List<PersistedCodeListItem>> loader = new CacheLoader<Key, List<PersistedCodeListItem>>() {
            public List<PersistedCodeListItem> load(Key key) {
                return loadFromDatabase(key.codeList, key.parentItemId);
            }
        };
        cache = CacheBuilder.newBuilder()
                .maximumWeight(ONE_MILLION_ITEMS)
                .weigher(new Weigher<Key, List<PersistedCodeListItem>>() {
                    public int weigh(Key key, List<PersistedCodeListItem> items) {
                        return items.size();
                    }
                })
                .build(loader);
    }

    public List<PersistedCodeListItem> load(final CodeList codeList, final Integer parentItemId) {
        return cache.getUnchecked(new Key(codeList, parentItemId));
    }

    private List<PersistedCodeListItem> loadFromDatabase(final CodeList codeList, final Integer parentItemId) {
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


    private static class Key {
        public final CodeList codeList;
        public final Integer parentItemId;

        public Key(CodeList codeList, Integer parentItemId) {
            this.codeList = codeList;
            this.parentItemId = parentItemId;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;

            if (codeList != null ? !codeList.equals(that.codeList) : that.codeList != null) return false;
            if (parentItemId != null ? !parentItemId.equals(that.parentItemId) : that.parentItemId != null)
                return false;

            return true;
        }

        public int hashCode() {
            int result = codeList != null ? codeList.hashCode() : 0;
            result = 31 * result + (parentItemId != null ? parentItemId.hashCode() : 0);
            return result;
        }
    }
}
