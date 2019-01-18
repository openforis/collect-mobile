package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.DatabaseLookupProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class MobileDatabaseLookupProvider extends DatabaseLookupProvider {
    private final Database database;

    public MobileDatabaseLookupProvider(Database database) {
        this.database = database;
    }

    @Override
    protected Object loadValue(String tableName, final String attribute, NameValueEntry[] filters) {
        final List<Object> params = new ArrayList<Object>();
        final StringBuilder query = new StringBuilder("SELECT ")
                .append(attribute)
                .append(" FROM ").append(tableName)
                .append(" WHERE 1 = 1 ");
        for (NameValueEntry filter : filters) {
            String colName = filter.getKey();
            query.append(" AND ").append(colName);

            Object value = normalizeFilterValue(filter.getValue());
            if (value == null)
                query.append(" IS NULL");
            else {
                query.append(" = ?");
                params.add(value);
            }
        }

        return database.execute(new ConnectionCallback<Object>() {
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
    }

    private Object normalizeFilterValue(Object value) {
        if (value instanceof String)
            return ((String) value).trim().isEmpty() ? null : value;
        return value;
    }
}
