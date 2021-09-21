package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;

import java.util.ArrayList;
import java.util.List;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

public class ChildCodeListItemsRepository extends AbstractCodeListItemRepository {
    public static final int ONE_MILLION_ITEMS = 1000 * 1000;
    private LruCache<Key, List<PersistedCodeListItem>> cache;
    private final AndroidDatabase database;

    public ChildCodeListItemsRepository(AndroidDatabase database) {
        this.database = database;
        this.cache = new LruCache<Key, List<PersistedCodeListItem>>(ONE_MILLION_ITEMS) {
            @Nullable
            @Override
            protected List<PersistedCodeListItem> create(@NonNull Key key) {
                return loadFromDatabase(key.codeList, key.parentItemId);
            }

            @Override
            protected int sizeOf(@NonNull Key key, @NonNull List<PersistedCodeListItem> items) {
                return items.size();
            }
        };
    }

    public List<PersistedCodeListItem> load(final CodeList codeList, final Long parentItemId) {
        return cache.get(new Key(codeList, parentItemId));
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
            return new ToStringBuilder(this)
                    .append("codeList", codeList)
                    .append("parentItemId", parentItemId)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return new EqualsBuilder().append(codeList, key.codeList).append(parentItemId, key.parentItemId).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(codeList).append(parentItemId).toHashCode();
        }
    }
}
