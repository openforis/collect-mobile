package org.openforis.collect.android.sqlite;

import io.requery.android.database.sqlite.SQLiteDatabase;
import org.openforis.collect.android.util.persistence.SchemaChange;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class NodeSchemaChangeLog {
    private final List<SchemaChange> changes;

    public NodeSchemaChangeLog(List<SchemaChange> changes) {
        this.changes = Collections.unmodifiableList(changes);
    }

    public final int getVersion() {
        return changes.size() + 1;
    }

    public final void init(SQLiteDatabase db) {
        apply(db, 1, getVersion());
    }

    public final void apply(SQLiteDatabase db) {
        int oldVersion = db.getVersion();
        apply(db, oldVersion, getVersion());
    }

    public final void apply(SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (changes.isEmpty())
            return;
        for (int i = oldVersion; i < newVersion; i++)
            execute(changes.get(i - 1), db);
    }

    private void execute(SchemaChange schemaChange, SQLiteDatabase db) {
        for (String statement : schemaChange.statements())
            db.execSQL(statement);
    }
}
