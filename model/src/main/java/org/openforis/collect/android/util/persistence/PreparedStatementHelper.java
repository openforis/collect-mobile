package org.openforis.collect.android.util.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author Daniel Wiell
 */
public class PreparedStatementHelper {
    private final PreparedStatement ps;
    private int i;

    public PreparedStatementHelper(PreparedStatement ps) {
        this.ps = ps;
    }

    public void setInt(int value) throws SQLException {
        ps.setInt(++i, value);
    }

    public void setBoolean(boolean value) throws SQLException {
        ps.setBoolean(++i, value);
    }

    public void setString(String value) throws SQLException {
        ps.setString(++i, value);
    }

    public void setIntOrNull(Integer value) throws SQLException {
        if (value == null) ps.setNull(++i, Types.INTEGER);
        else ps.setInt(++i, value);
    }

    public void setLongOrNull(Long value) throws SQLException {
        if (value == null) ps.setNull(++i, Types.INTEGER);
        else ps.setLong(++i, value);
    }

    public void setDouble(double value) throws SQLException {
        ps.setDouble(++i, value);
    }

    public void setDoubleOrNull(Double value) throws SQLException {
        if (value == null) ps.setNull(++i, Types.REAL);
        else ps.setDouble(++i, value);
    }

    public void setBooleanOrNull(Boolean value) throws SQLException {
        if (value == null) ps.setNull(++i, Types.BOOLEAN);
        else ps.setBoolean(++i, value);
    }

    public void setStringOrNull(String value) throws SQLException {
        if (value == null) ps.setNull(++i, Types.VARCHAR);
        else ps.setString(++i, value);
    }
}
