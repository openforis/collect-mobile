package org.openforis.collect.android.viewmodelmanager;


import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.util.persistence.PreparedStatementHelper;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class DataSourceNodeRepository implements NodeRepository {
    private final Database database;

    public DataSourceNodeRepository(Database database) {
        this.database = database;
        IdGenerator.setLastId(lastId());
        database.execute(new ConnectionCallback<Object>() {
            public Object execute(Connection connection) throws SQLException {
                connection.createStatement().execute("delete from ofc_view_model");
                return null;
            }
        });
    }

    private int lastId() {
        return database.execute(new ConnectionCallback<Integer>() {
            public Integer execute(Connection connection) throws SQLException {
                ResultSet rs = connection.createStatement().executeQuery("SELECT MAX(id) FROM ofc_view_model");
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    public void insert(final List<NodeDto> nodes) {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("" +
                        "INSERT INTO ofc_view_model(\n" +
                        "   relevant, status, parent_id, parent_entity_id, definition_id, survey_id, record_id, record_collection_name,\n" +
                        "   record_key_attribute, node_type,\n" +
                        "   val_text, val_date, val_hour, val_minute, val_code_value, val_code_label,\n" +
                        "   val_boolean, val_int, val_int_from, val_int_to,\n" +
                        "   val_double, val_double_from, val_double_to, val_x, val_y, val_taxon_code,\n" +
                        "   val_taxon_scientific_name, val_file, id)\n" +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (NodeDto node : nodes) {
                    bind(ps, node);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
                return null;
            }
        });
    }


    public void removeAll(final List<Integer> ids) {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("DELETE FROM ofc_view_model WHERE id = ?");
                for (int id : ids) {
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
                return null;
            }
        });
    }

    public void removeRecord(final int recordId) {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("DELETE FROM ofc_view_model WHERE record_id = ?");
                ps.setInt(1, recordId);
                ps.executeUpdate();
                ps.close();
                return null;
            }
        });
    }

    public NodeDto.Collection recordNodes(final int recordId) {
        return database.execute(new ConnectionCallback<NodeDto.Collection>() {
            public NodeDto.Collection execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT id, relevant, status, parent_id, parent_entity_id, survey_id, record_id, definition_id,\n" +
                        "       record_collection_name, record_key_attribute, node_type,\n" +
                        "       val_text, val_date, val_hour, val_minute, val_code_value, val_code_label,\n" +
                        "       val_boolean, val_int, val_int_from,\n" +
                        "       val_int_to, val_double, val_double_from, val_double_to, val_x, val_y,\n" +
                        "       val_taxon_code, val_taxon_scientific_name, val_file\n" +
                        "FROM ofc_view_model\n" +
                        "WHERE record_id = ?\n" +
                        "ORDER BY id");
                ps.setInt(1, recordId);
                ResultSet rs = ps.executeQuery();
                NodeDto.Collection collection = new NodeDto.Collection();
                while (rs.next())
                    collection.addNode(toNode(rs));
                rs.close();
                ps.close();
                return collection;
            }
        });
    }

    public void update(final NodeDto node, final List<Map<String, Object>> statusChanges, final String recordStatus) {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                updateAttribute(connection, node);
                if (recordStatus != null)
                    updateRecordStatus(connection, node.recordId, recordStatus);
                updateStatusChanges(connection, statusChanges);
                return null;
            }
        });
    }

    private void updateStatusChanges(Connection connection, List<Map<String, Object>> statusChanges) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("" +
                "UPDATE ofc_view_model\n" +
                "SET relevant = ?, status = ?\n" +
                "WHERE id = ?");
        for (Map<String, Object> statusChange : statusChanges) {
            ps.setBoolean(1, (Boolean) statusChange.get("relevant"));
            ps.setString(2, (String) statusChange.get("status"));
            ps.setInt(3, (Integer) statusChange.get("id"));
            ps.addBatch();
        }
        ps.executeBatch();
        ps.close();
    }

    private void updateRecordStatus(Connection connection, int recordId, String recordStatus) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("UPDATE ofc_view_model SET status =? WHERE id = ?");
        ps.setString(1, recordStatus);
        ps.setInt(2, recordId);
        ps.executeUpdate();
        ps.close();
    }

    private void updateAttribute(Connection connection, NodeDto node) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("" +
                "UPDATE ofc_view_model\n" +
                "SET relevant = ?, status = ?, parent_id = ?, parent_entity_id = ?, definition_id = ?, survey_id = ?, record_id = ?,\n" +
                "    record_collection_name = ?, record_key_attribute = ?, node_type = ?, val_text = ?, val_date = ?, val_hour = ?,\n" +
                "    val_minute = ?, val_code_value = ?, val_code_label = ?, val_boolean = ?, val_int = ?, val_int_from = ?,\n" +
                "    val_int_to = ?, val_double = ?, val_double_from = ?, val_double_to = ?, val_x = ?,\n" +
                "    val_y = ?, val_taxon_code = ?, val_taxon_scientific_name = ?,\n" +
                "    val_file = ?\n" +
                "WHERE id = ?");
        bind(ps, node);
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1)
            throw new IllegalStateException("Expected exactly one row to be updated. Was " + rowsUpdated);
        ps.close();
    }

    public NodeDto.Collection surveyRecords(final int surveyId) {
        return database.execute(new ConnectionCallback<NodeDto.Collection>() {
            public NodeDto.Collection execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT id, relevant, status, parent_id, parent_entity_id, survey_id, record_id, definition_id, record_collection_name,\n" +
                        "       record_key_attribute, node_type,\n" +
                        "       val_text, val_date, val_hour, val_minute, val_code_value, val_code_label,\n" +
                        "       val_boolean, val_int, val_int_from,\n" +
                        "       val_int_to, val_double, val_double_from, val_double_to, val_x, val_y,\n" +
                        "       val_taxon_code, val_taxon_scientific_name, val_file\n" +
                        "FROM ofc_view_model\n" +
                        "WHERE survey_id = ? AND (parent_id IS NULL OR record_key_attribute = ?)\n" +
                        "ORDER BY id");
                ps.setInt(1, surveyId);
                ps.setInt(2, 1);
                ResultSet rs = ps.executeQuery();
                NodeDto.Collection collection = new NodeDto.Collection();
                while (rs.next()) {
                    NodeDto node = toNode(rs);
                    if (node.parentId != null)
                        node.parentId = node.recordId; // Put key attributes directly under record // TODO: Ugly!
                    collection.addNode(node);
                }
                rs.close();
                ps.close();
                return collection;
            }
        });
    }

    private NodeDto toNode(ResultSet rs) throws SQLException {
        NodeDto n = new NodeDto();
        n.id = rs.getInt("id");
        n.relevant = rs.getBoolean("relevant");
        n.status = rs.getString("status");
        n.parentId = getInteger("parent_id", rs);
        n.parentEntityId = getInteger("parent_entity_id", rs);
        n.definitionId = rs.getString("definition_id");
        n.surveyId = rs.getInt("survey_id");
        n.recordId = rs.getInt("record_id");
        n.recordCollectionName = rs.getString("record_collection_name");
        n.recordKeyAttribute = rs.getBoolean("record_key_attribute");
        n.type = NodeDto.Type.byId(rs.getInt("node_type"));
        n.text = rs.getString("val_text");
        Long dateMillis = getLong("val_date", rs);
        n.date = dateMillis == null ? null : new Date(dateMillis);
        n.hour = getInteger("val_hour", rs);
        n.minute = getInteger("val_minute", rs);
        n.codeValue = rs.getString("val_code_value");
        n.codeLabel = rs.getString("val_code_label");
        n.booleanValue = getBoolean("val_boolean", rs);
        n.intValue = getInteger("val_int", rs);
        n.intFrom = getInteger("val_int_from", rs);
        n.intTo = getInteger("val_int_to", rs);
        n.doubleValue = getDouble("val_double", rs);
        n.doubleFrom = getDouble("val_double_from", rs);
        n.doubleTo = getDouble("val_double_to", rs);
        n.x = getDouble("val_x", rs);
        n.y = getDouble("val_y", rs);
        n.taxonCode = rs.getString("val_taxon_code");
        n.taxonScientificName = rs.getString("val_taxon_scientific_name");
        String filePath = rs.getString("val_file");
        n.file = filePath == null ? null : new File(filePath);
        return n;
    }

    private Integer getInteger(String columnName, ResultSet rs) throws SQLException {
        int value = rs.getInt(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    private Long getLong(String columnName, ResultSet rs) throws SQLException {
        long value = rs.getLong(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    private Double getDouble(String columnName, ResultSet rs) throws SQLException {
        double value = rs.getDouble(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    private Boolean getBoolean(String columnName, ResultSet rs) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    private void bind(PreparedStatement ps, NodeDto node) throws SQLException {
        PreparedStatementHelper psh = new PreparedStatementHelper(ps);
        psh.setBoolean(node.relevant);
        psh.setString(node.status);
        psh.setIntOrNull(node.parentId);
        psh.setIntOrNull(node.parentEntityId);
        psh.setString(node.definitionId);
        psh.setInt(node.surveyId);
        psh.setInt(node.recordId);
        psh.setString(node.recordCollectionName);
        psh.setBoolean(node.recordKeyAttribute);
        psh.setInt(node.type.id);
        psh.setString(node.text);
        psh.setLongOrNull(node.date == null ? null : node.date.getTime());
        psh.setIntOrNull(node.hour);
        psh.setIntOrNull(node.minute);
        psh.setString(node.codeValue);
        psh.setString(node.codeLabel);
        psh.setBooleanOrNull(node.booleanValue);
        psh.setIntOrNull(node.intValue);
        psh.setIntOrNull(node.intFrom);
        psh.setIntOrNull(node.intTo);
        psh.setDoubleOrNull(node.doubleValue);
        psh.setDoubleOrNull(node.doubleFrom);
        psh.setDoubleOrNull(node.doubleTo);
        psh.setDoubleOrNull(node.x);
        psh.setDoubleOrNull(node.y);
        psh.setString(node.taxonCode);
        psh.setString(node.taxonScientificName);
        psh.setStringOrNull(node.file == null ? null : node.file.getAbsolutePath());
        psh.setInt(node.id);
    }
}
