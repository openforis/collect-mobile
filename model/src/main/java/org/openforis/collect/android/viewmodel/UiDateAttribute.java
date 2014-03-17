package org.openforis.collect.android.viewmodel;

import java.util.Date;

/**
 * @author Daniel Wiell
 */
public class UiDateAttribute extends UiAttribute {
    private Date date = new Date();

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
}
