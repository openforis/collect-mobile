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

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

public class CodeListItemRepository extends AbstractCodeListItemRepository {
    public static final int ONE_MILLION_ITEMS = 1000 * 1000;
    private final LruCache<Key, PersistedCodeListItem> cache;
    private final AndroidDatabase database;

    public CodeListItemRepository(AndroidDatabase database) {
        this.database = database;
        this.cache = new LruCache<Key, PersistedCodeListItem>(ONE_MILLION_ITEMS) {
            @Nullable
            @Override
            protected PersistedCodeListItem create(@NonNull Key key) {
                return loadFromDatabase(key.codeList, key.parentItemId, key.code);
            }
        };
    }

    public PersistedCodeListItem load(CodeList codeList, Long parentItemId, String code) {
        return cache.get(new Key(codeList, parentItemId, code));
    }

    private PersistedCodeListItem loadFromDatabase(final CodeList codeList, final Long parentItemId, final String code) {
        return database.execute(new AndroidDatabaseCallback<PersistedCodeListItem>() {
            public PersistedCodeListItem execute(SQLiteDatabase database) {
                String[] params = code == null ? null : new String[]{code};
                Cursor cursor = database.rawQuery("" +
                        "select * from " + OFC_CODE_LIST.getName()
                        + " where " + OFC_CODE_LIST.CODE_LIST_ID.getName() + constraint(codeList.getId())
                        + " and " + OFC_CODE_LIST.PARENT_ID.getName() + constraint(parentItemId)
                        + " and " + OFC_CODE_LIST.CODE.getName() + parameterizedConstraint(code), params);
                try {
                    if (!cursor.moveToFirst())
                        return null;
                    return createCodeListItem(cursor, codeList);
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        });
    }

    private static class Key {
        private final CodeList codeList;
        private final Long parentItemId;
        private final String code;

        public Key(CodeList codeList, Long parentItemId, String code) {
            this.codeList = codeList;
            this.parentItemId = parentItemId;
            this.code = code;
        }

        public String toString() {
            return new ToStringBuilder(this)
                    .append("codeList", codeList)
                    .append("parentItemId", parentItemId)
                    .append("code", code)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return new EqualsBuilder().append(codeList, key.codeList).append(parentItemId, key.parentItemId).append(code, key.code).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(codeList).append(parentItemId).append(code).toHashCode();
        }
    }

    private static class ItemNotFound extends RuntimeException {

    }

}