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
                            .append(constrain(key))
                            .append(")");
                    if (iterator.hasNext())
                        constraint.append(" OR");
                }
                return constraint.toString();
            }

            // TODO: Need some design - switch statement is repeated three times - NodeDto.recordKeyAttribute and two times here.
            private String constrain(NodeDto key) {
                switch (key.type) {
                    case CODE_ATTRIBUTE:
                        return "val_code_value = ?";
                    case DOUBLE_ATTRIBUTE:
                        return "val_double = ?";
                    case INTEGER_ATTRIBUTE:
                        return "val_int = ?";
                    case TEXT_ATTRIBUTE:
                        return "val_text = ?";
                    default:
                        throw new IllegalStateException("Attribute type cannot be record key: " + key.type);
                }
            }

            private void bind(PreparedStatement ps) throws SQLException {
                PreparedStatementHelper psh = new PreparedStatementHelper(ps);
                psh.setInt(keys.get(0).recordId); // The recordId is the same for all keys - pick it from the first
                for (NodeDto key : keys) {
                    psh.setInt(Integer.parseInt(key.definitionId));
                    switch (key.type) {
                        case CODE_ATTRIBUTE:
                            psh.setStringOrNull(key.codeValue);
                            break;
                        case DOUBLE_ATTRIBUTE:
                            psh.setDoubleOrNull(key.doubleValue);
                            break;
                        case INTEGER_ATTRIBUTE:
                            psh.setIntOrNull(key.intValue);
                            break;
                        case TEXT_ATTRIBUTE:
                            psh.setStringOrNull(key.text);
                            break;
                        default:
                            throw new IllegalStateException("Attribute type cannot be record key: " + key.type);
                    }
                }
            }
        }
    }
}
