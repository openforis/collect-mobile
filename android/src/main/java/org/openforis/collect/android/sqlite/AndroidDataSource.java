package org.openforis.collect.android.sqlite;


import org.sqldroid.SQLDroidDriver;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author Daniel Wiell
 * @author S. Ricci
 */
class AndroidDataSource implements DataSource {
    private final String url;
//    private Connection connection; // TODO: Ugly - sharing the connection

    AndroidDataSource(File databaseFile) {
        this.url = "jdbc:sqldroid:" + databaseFile.getAbsolutePath();
        registerDriver();
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
//        if (connection == null || connection.isClosed())
//            connection = DriverManager.getConnection(url);
//        return connection;
        return DriverManager.getConnection(url);
    }

    @Override
    public Connection getConnection(String username, String password)
            throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        return null;
    }

    // Compatibility with JDK 7
    public Logger getParentLogger() {
        return null;
    }

    private void registerDriver() {
        try {
            Class.forName(SQLDroidDriver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e); // Should never happen
        }
    }
}
