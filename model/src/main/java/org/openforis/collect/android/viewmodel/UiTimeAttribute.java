package org.openforis.collect.android.viewmodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Daniel Wiell
 */
public class UiTimeAttribute extends UiAttribute {
    private int hour;
    private int minute;

    public UiTimeAttribute(int id, Definition definition) {
        super(id, definition);
        setCurrentTime();
    }

    public synchronized int getHour() {
        return hour;
    }

    public synchronized int getMinute() {
        return minute;
    }

    public synchronized void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    private void setCurrentTime() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }
}
