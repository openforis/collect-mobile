package org.openforis.collect.android.sqlite;

import io.requery.android.database.sqlite.SQLiteDatabase;

/**
 * @author Daniel Wiell
 */
public interface AndroidDatabaseCallback<T> {
    T execute(SQLiteDatabase database);
}
