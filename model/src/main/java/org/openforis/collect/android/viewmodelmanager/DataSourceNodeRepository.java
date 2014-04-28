package org.openforis.collect.android.viewmodelmanager;


import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;

import java.io.File;
import java.sql.*;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class DataSourceNodeRepository implements NodeRepository {
    private final Database database;

    public DataSourceNodeRepository(Database database) {
        this.database = database;
        IdGenerator.setLastId(lastId());
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

    public void update(final NodeDto node, final String recordStatus) {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                updateAttribute(connection, node);
                if (recordStatus != null)
                    updateRecordStatus(connection, node.recordId, recordStatus);
                return null;
            }
        });
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
        int i = 0;
        ps.setBoolean(++i, node.relevant);
        ps.setString(++i, node.status);
        setIntOrNull(++i, node.parentId, ps);
        setIntOrNull(++i, node.parentEntityId, ps);
        ps.setString(++i, node.definitionId);
        ps.setInt(++i, node.surveyId);
        ps.setInt(++i, node.recordId);
        ps.setString(++i, node.recordCollectionName);
        ps.setBoolean(++i, node.recordKeyAttribute);
        ps.setInt(++i, node.type.id);
        ps.setString(++i, node.text);
        setIntOrNull(++i, node.date == null ? null : node.date.getTime(), ps);
        setIntOrNull(++i, node.hour, ps);
        setIntOrNull(++i, node.minute, ps);
        ps.setString(++i, node.codeValue);
        ps.setString(++i, node.codeLabel);
        setBooleanOrNull(++i, node.booleanValue, ps);
        setIntOrNull(++i, node.intValue, ps);
        setIntOrNull(++i, node.intFrom, ps);
        setIntOrNull(++i, node.intTo, ps);
        setDoubleOrNull(++i, node.doubleValue, ps);
        setDoubleOrNull(++i, node.doubleFrom, ps);
        setDoubleOrNull(++i, node.doubleTo, ps);
        setDoubleOrNull(++i, node.x, ps);
        setDoubleOrNull(++i, node.y, ps);
        ps.setString(++i, node.taxonCode);
        ps.setString(++i, node.taxonScientificName);
        setStringOrNull(++i, node.file == null ? null : node.file.getAbsolutePath(), ps);
        ps.setInt(++i, node.id);
    }

    private void setIntOrNull(int i, Number value, PreparedStatement ps) throws SQLException {
        if (value == null) ps.setNull(i, Types.INTEGER);
        else ps.setLong(i, value.longValue());
    }

    private void setDoubleOrNull(int i, Double value, PreparedStatement ps) throws SQLException {
        if (value == null) ps.setNull(i, Types.REAL);
        else ps.setDouble(i, value);
    }

    private void setBooleanOrNull(int i, Boolean value, PreparedStatement ps) throws SQLException {
        if (value == null) ps.setNull(i, Types.BOOLEAN);
        else ps.setBoolean(i, value);
    }

    private void setStringOrNull(int i, String value, PreparedStatement ps) throws SQLException {
        if (value == null) ps.setNull(i, Types.VARCHAR);
        else ps.setString(i, value);
    }
}
