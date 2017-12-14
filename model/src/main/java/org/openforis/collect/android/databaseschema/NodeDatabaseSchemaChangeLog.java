package org.openforis.collect.android.databaseschema;

import org.openforis.collect.android.util.persistence.SchemaChange;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Daniel Wiell
 */
public class NodeDatabaseSchemaChangeLog {
    public List<SchemaChange> changes() {
        return asList(
                new SchemaChange("CREATE TABLE ofc_view_model(\n" +
                        "id INTEGER PRIMARY KEY NOT NULL,\n" +
                        "relevant INTEGER NOT NULL,\n" +
                        "status TEXT NOT NULL,\n" +
                        "parent_id INTEGER,\n" +
                        "parent_entity_id INTEGER,\n" +
                        "definition_id TEXT NOT NULL,\n" +
                        "survey_id INTEGER NOT NULL,\n" +
                        "record_id INTEGER NOT NULL,\n" +
                        "record_collection_name TEXT,\n" +
                        "record_key_attribute INTEGER,\n" +
                        "node_type INTEGER NOT NULL,\n" +
                        "val_text TEXT,\n" +
                        "val_date INTEGER,\n" +
                        "val_hour INTEGER,\n" +
                        "val_minute INTEGER,\n" +
                        "val_code_value TEXT,\n" +
                        "val_code_qualifier TEXT,\n" +
                        "val_code_label TEXT,\n" +
                        "val_boolean INTEGER,\n" +
                        "val_int INTEGER,\n" +
                        "val_int_from INTEGER,\n" +
                        "val_int_to INTEGER,\n" +
                        "val_double REAL,\n" +
                        "val_double_from REAL,\n" +
                        "val_double_to REAL,\n" +
                        "val_x REAL,\n" +
                        "val_y REAL,\n" +
                        "val_srs TEXT,\n" +
                        "val_taxon_code TEXT,\n" +
                        "val_taxon_scientific_name TEXT,\n" +
                        "val_file TEXT\n" +
                        ")"),

                new SchemaChange("CREATE INDEX idx_ofc_view_model_1 ON ofc_view_model(record_id)"),

                new SchemaChange("ALTER TABLE ofc_view_model ADD COLUMN created_on TIMESTAMP",
                        "UPDATE ofc_view_model SET created_on = CURRENT_TIMESTAMP",
                        "ALTER TABLE ofc_view_model ADD COLUMN modified_on TIMESTAMP ",
                        "UPDATE ofc_view_model SET created_on = CURRENT_TIMESTAMP")
        );
    }
}
