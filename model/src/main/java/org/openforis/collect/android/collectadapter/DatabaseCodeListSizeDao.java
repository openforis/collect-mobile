package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Daniel Wiell
 */
public class DatabaseCodeListSizeDao implements CodeListSizeDao {
    private final Database database;

    public DatabaseCodeListSizeDao(Database database) {
        this.database = database;
    }

    public int codeListSize(final int codeListId, final int level) {
        return database.execute(new ConnectionCallback<Integer>() {
            public Integer execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT MAX(count) " +
                        "FROM \n" +
                        "   (SELECT COUNT(*) count\n" +
                        "   FROM ofc_code_list\n" +
                        "   WHERE code_list_id = ? AND level = ?" +
                        "   GROUP BY parent_id) s");
                ps.setInt(1, codeListId);
                ps.setInt(2, level);
                ResultSet rs = ps.executeQuery();
                rs.next();
                return rs.getInt(1);
            }
        });
    }

    public int externalCodeListSize(int codeListId, int level) {
        return Integer.MAX_VALUE; // TODO: Implement...
    }
}
