package org.openforis.collect.android.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Wiell
 */
public class UiDateAttribute extends UiAttribute {
    public static final String DATE_PATTERN = "dd MMMM yyyy";
    private Date date;

    public UiDateAttribute(int id, Definition definition) {
        super(id, definition);
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

    public String toString() {
        return date == null ? "Undefined date" : new SimpleDateFormat(DATE_PATTERN).format(date);
    }
}
