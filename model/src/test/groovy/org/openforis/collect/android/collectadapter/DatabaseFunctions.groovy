package org.openforis.collect.android.collectadapter

import java.sql.Connection
import java.sql.SQLException

/**
 * @author Daniel Wiell
 */
class DatabaseFunctions {
    static Long setSequenceValue(Connection conn, String sequenceName, Long value, Boolean isCalled) throws SQLException {
//            boolean isCalledValue = isCalled == null ? true : isCalled.booleanValue()
        conn.createStatement().execute("ALTER SEQUENCE " + sequenceName + " RESTART WITH " + value)
        return value
    }
}
