package org.openforis.collect.android.util.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static java.util.Locale.ENGLISH;

public class ResultSetHelper {

    private static final SimpleDateFormat[] TIMESTAMP_FORMATTERS = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S", ENGLISH), //default timestamp format
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", ENGLISH)}; //for timestamp set with CURRENT_TIMESTAMP

    private final ResultSet rs;

    public ResultSetHelper(ResultSet rs) {
        this.rs = rs;
    }

    public Integer getInteger(String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    public Long getLong(String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    public Double getDouble(String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    public Boolean getBoolean(String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        if (rs.wasNull())
            return null;
        return value;
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        //TODO use newer version of SQLDroid
        // current version 1.0.3 has a bug: it doesn't consider missing milliseconds in timestamp
        //return rs.getTimestamp(columnName);

        String timestampStr = rs.getString(columnName);

        if (timestampStr == null) {
            return null;
        } else {
            for (SimpleDateFormat formatter: TIMESTAMP_FORMATTERS)
                try {
                    java.util.Date parsedTimestamp = formatter.parse(timestampStr);
                    return new Timestamp(parsedTimestamp.getTime());
                } catch (ParseException e) {
                    //DO NOTHING
                }
            return null;
        }
    }
}
