package org.openforis.collect.android.util.persistence;

import javax.sql.DataSource;

/**
 * @author Daniel Wiell
 */
public interface Database {
    DataSource dataSource();

    <T> T execute(ConnectionCallback<T> connectionCallback);
}
