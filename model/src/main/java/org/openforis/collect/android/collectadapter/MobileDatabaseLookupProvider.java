package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class MobileDatabaseLookupProvider implements LookupProvider {
    private final Database database;

    public MobileDatabaseLookupProvider(Database database) {
        this.database = database;
    }

    public Object lookup(String tableName, final String attribute, Object... columns) {
        final StringBuilder query = new StringBuilder("SELECT ")
                .append(attribute)
                .append(" FROM ").append(tableName)
                .append(" WHERE 1 = 1 ");

        NameValueEntry[] filters = NameValueEntry.fromKeyValuePairs(columns);
        final List<Object> params = new ArrayList<Object>();
        for (NameValueEntry filter : filters) {
            String colName = filter.getKey();
            Object value = filter.getValue();
            if (value instanceof String)
                value = ((String) value).trim().isEmpty() ? null : value;
            query.append(" AND ").append(colName);
            if (value == null) {
                query.append(" IS NULL");
            } else {
                query.append(" = ?");
                params.add(value);
            }
        }
        System.out.println(query);

        Object result = database.execute(new ConnectionCallback<Object>() {
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(query.toString());
                for (int i = 0; i < params.size(); i++)
                    ps.setObject(i + 1, params.get(i));
                ResultSet rs = ps.executeQuery();
                Object result = rs.next() ? rs.getObject(attribute) : null;
                rs.close();
                ps.close();
                return result;
            }
        });
        if (result != null) {
            Coordinate coordinate = Coordinate.parseCoordinate(result.toString());
            return coordinate;
        }
        return null;
    }
}
