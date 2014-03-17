package org.openforis.collect.android.util.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Daniel Wiell
 */
public interface ConnectionCallback<T> {
    T execute(Connection connection) throws SQLException;
}
