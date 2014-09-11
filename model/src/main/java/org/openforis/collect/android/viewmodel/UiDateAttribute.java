package org.openforis.collect.android.viewmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Wiell
 */
public class UiDateAttribute extends UiAttribute {
    private static final String DATE_PATTERN = "dd MMMM yyyy";
    private Date date;

    public UiDateAttribute(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public boolean isEmpty() {
        return date == null;
    }

    public synchronized Date getDate() {
        return date;
    }

    public synchronized void setDate(Date date) {
        this.date = date;
    }

    public String valueAsString() {
        return date == null ? null : new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static String format(Date date) {
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static Date parse(String newValue) throws ParseException {
        return new SimpleDateFormat(DATE_PATTERN).parse(newValue);
    }

    public String toString() {
        return date == null ? "Undefined date" : new SimpleDateFormat(DATE_PATTERN).format(date);
    }
}
