package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.util.persistence.PreparedStatementHelper;
import org.openforis.collect.android.viewmodelmanager.NodeDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface RecordUniquenessChecker {
    boolean isUnique(List<NodeDto> keys);


    class DataSourceRecordUniquenessChecker implements RecordUniquenessChecker {
        private Database database;

        public DataSourceRecordUniquenessChecker(Database database) {
            this.database = database;
        }

        public boolean isUnique(List<NodeDto> keys) {
            return database.execute(new RecordKeyUniquenessCheck(keys));
        }

        private static class RecordKeyUniquenessCheck implements ConnectionCallback<Boolean> {
            private final List<NodeDto> keys;

            public RecordKeyUniquenessCheck(List<NodeDto> keys) {
                if (keys == null || keys.isEmpty())
                    throw new IllegalArgumentException("Expected to have at least one key");
                this.keys = keys;
            }

            public Boolean execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(query());
                bind(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    return isUnique(rs);
                } finally {
                    rs.close();
                    ps.close();
                }
            }

            private String query() {
                return "SELECT record_id, count(*) matching_attributes\n" +
                        "FROM ofc_view_model\n" +
                        "WHERE record_id != ? AND (\n" +
                        constraints() +
                        ")\n" +
                        "GROUP BY record_id";
            }

            private Boolean isUnique(ResultSet rs) throws SQLException {
                while (rs.next())
                    if (rs.getInt("matching_attributes") == keys.size())
                        return false;
                return true;
            }

            private String constraints() {
                StringBuilder constraint = new StringBuilder();
                for (Iterator<NodeDto> iterator = keys.iterator(); iterator.hasNext(); ) {
                    NodeDto key = iterator.next();
                    constraint.append(" (definition_id = ? AND ")
                            .append(constraint(key))
                            .append(")");
                    if (iterator.hasNext())
                        constraint.append(" OR");
                }
                return constraint.toString();
            }

            // TODO: Need some design - switch statement is repeated three times - NodeDto.recordKeyAttribute and two times here.
            private String constraint(NodeDto key) {
                // SQLDroid uses SQLite rawQuery, which does not allow null values to be bound.
                switch (key.type) {
                    case CODE_ATTRIBUTE:
                        return key.codeValue == null ? "val_code_value IS NULL" : "val_code_value = ?";
                    case COORDINATE_ATTRIBUTE: {
                        String constraint = "(";
                        constraint += key.x == null ? "val_x IS NULL" : "val_x = ?";
                        constraint += " AND ";
                        constraint += key.y == null ? "val_y IS NULL" : "val_y = ?";
                        constraint += " AND ";
                        constraint += key.srs == null ? "val_srs IS NULL" : "val_srs = ?";
                        constraint += ")";
                        return constraint;
                    }
                    case DATE_ATTRIBUTE:
                        return key.date == null ? "val_date IS NULL" : "val_date = ?";
                    case DOUBLE_ATTRIBUTE:
                        return key.doubleValue == null ? "val_double IS NULL" : "val_double = ?";
                    case INTEGER_ATTRIBUTE:
                        return key.intValue == null ? "val_int IS NULL" : "val_int = ?";
                    case TEXT_ATTRIBUTE:
                        return key.text == null ? "val_text IS NULL" : "val_text = ?";
                    case TIME_ATTRIBUTE: {
                        String constraint = "(";
                        constraint += key.hour == null ? "val_hour IS NULL" : "val_hour = ?";
                        constraint += " AND ";
                        constraint += key.minute == null ? "val_minute IS NULL" : "val_minute = ?";
                        constraint += ")";
                        return constraint;
                    }
                    default:
                        throw new IllegalStateException("Attribute type cannot be record key: " + key.type);
                }
            }

            private void bind(PreparedStatement ps) throws SQLException {
                PreparedStatementHelper psh = new PreparedStatementHelper(ps);
                psh.setInt(keys.get(0).recordId); // The recordId is the same for all keys - pick it from the first
                for (NodeDto key : keys) {
                    psh.setInt(Integer.parseInt(key.definitionId));
                    // SQLDroid uses SQLite rawQuery, which does not allow null values to be bound.
                    switch (key.type) {
                        case CODE_ATTRIBUTE:
                            psh.setStringIfNotNull(key.codeValue);
                            break;
                        case COORDINATE_ATTRIBUTE:
                            psh.setDoubleIfNotNull(key.x);
                            psh.setDoubleIfNotNull(key.y);
                            psh.setStringIfNotNull(key.srs);
                            break;
                        case DATE_ATTRIBUTE:
                            if (key.date != null)
                                psh.setLongOrNull(key.date.getTime());
                            break;
                        case DOUBLE_ATTRIBUTE:
                            psh.setDoubleIfNotNull(key.doubleValue);
                            break;
                        case INTEGER_ATTRIBUTE:
                            psh.setIntIfNotNull(key.intValue);
                            break;
                        case TEXT_ATTRIBUTE:
                            psh.setStringIfNotNull(key.text);
                            break;
                        case TIME_ATTRIBUTE:
                            psh.setIntIfNotNull(key.hour);
                            psh.setIntIfNotNull(key.minute);
                            break;
                        default:
                            throw new IllegalStateException("Attribute type cannot be record key: " + key.type);
                    }
                }
            }
        }
    }
}
