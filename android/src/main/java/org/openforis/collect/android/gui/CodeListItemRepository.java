package org.openforis.collect.android.gui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.sqlite.AndroidDatabaseCallback;
import org.openforis.collect.android.util.persistence.PersistenceException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;

import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

public class CodeListItemRepository extends AbstractCodeListItemRepository {
    public static final int ONE_MILLION_ITEMS = 1000 * 1000;
    private final LoadingCache<Key, PersistedCodeListItem> cache;
    private final AndroidDatabase database;

    public CodeListItemRepository(AndroidDatabase database) {
        this.database = database;
        CacheLoader<Key, PersistedCodeListItem> loader = new CacheLoader<Key, PersistedCodeListItem>() {
            public PersistedCodeListItem load(Key key) {
                PersistedCodeListItem result = loadFromDatabase(key.codeList, key.parentItemId, key.code);
                if (result == null)
                    throw new ItemNotFound();
                return result;
            }
        };
        cache = CacheBuilder.newBuilder()
                .maximumSize(ONE_MILLION_ITEMS)
                .build(loader);
    }

    public PersistedCodeListItem load(CodeList codeList, Integer parentItemId, String code) {
        try {
            return cache.getUnchecked(new Key(codeList, parentItemId, code));
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof ItemNotFound)
                return null;
            throw new PersistenceException(e);
        }
    }


    private PersistedCodeListItem loadFromDatabase(final CodeList codeList, final Integer parentItemId, final String code) {
        return database.execute(new AndroidDatabaseCallback<PersistedCodeListItem>() {
            public PersistedCodeListItem execute(SQLiteDatabase database) {
                Cursor cursor = database.rawQuery("" +
                        "select * from " + OFC_CODE_LIST
                        + " where " + OFC_CODE_LIST.CODE_LIST_ID + constraint(codeList.getId())
                        + " and " + OFC_CODE_LIST.PARENT_ID + constraint(parentItemId)
                        + " and " + OFC_CODE_LIST.CODE + constraint(code), null);
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
        private final Integer parentItemId;
        private final String code;

        public Key(CodeList codeList, Integer parentItemId, String code) {
            this.codeList = codeList;
            this.parentItemId = parentItemId;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (code != null ? !code.equals(key.code) : key.code != null) return false;
            if (codeList != null ? !codeList.equals(key.codeList) : key.codeList != null) return false;
            if (parentItemId != null ? !parentItemId.equals(key.parentItemId) : key.parentItemId != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = codeList != null ? codeList.hashCode() : 0;
            result = 31 * result + (parentItemId != null ? parentItemId.hashCode() : 0);
            result = 31 * result + (code != null ? code.hashCode() : 0);
            return result;
        }
    }

    private static class ItemNotFound extends RuntimeException {

    }
}
