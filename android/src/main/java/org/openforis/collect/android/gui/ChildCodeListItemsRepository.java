package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;

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

    public List<PersistedCodeListItem> load(final CodeList codeList, final Long parentItemId) {
        return cache.getUnchecked(new Key(codeList, parentItemId));
    }

    private List<PersistedCodeListItem> loadFromDatabase(final CodeList codeList, final Long parentItemId) {
        return database.execute(new AndroidDatabaseCallback<List<PersistedCodeListItem>>() {
            public List<PersistedCodeListItem> execute(SQLiteDatabase database) {
                Cursor cursor = database.rawQuery("" +
                        "select * from " + OFC_CODE_LIST.getName()
                        + " where " + OFC_CODE_LIST.CODE_LIST_ID.getName() + constraint(codeList.getId())
                        + " and " + OFC_CODE_LIST.PARENT_ID.getName() + constraint(parentItemId)
                        + " order by " + OFC_CODE_LIST.SORT_ORDER.getName(), null);
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
        public final Long parentItemId;

        public Key(CodeList codeList, Long parentItemId) {
            this.codeList = codeList;
            this.parentItemId = parentItemId;
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("codeList", codeList)
                    .add("parentItemId", parentItemId)
                    .toString();
        }

        public int hashCode() {
            return Objects.hashCode(codeList, parentItemId);
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Key other = (Key) obj;
            return Objects.equal(this.codeList, other.codeList)
                    && Objects.equal(this.parentItemId, other.parentItemId);
        }
    }
}
