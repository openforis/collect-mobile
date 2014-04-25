package org.openforis.collect.android.viewmodel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Daniel Wiell
 */
public class UiTimeAttribute extends UiAttribute {
    private Integer hour;
    private Integer minute;

    public UiTimeAttribute(int id, Definition definition) {
        super(id, definition);
        setCurrentTime();
    }

    public synchronized Integer getHour() {
        return hour;
    }

    public synchronized Integer getMinute() {
        return minute;
    }

    public synchronized void setTime(Integer hour, Integer minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public boolean isEmpty() {
        return hour == null || minute == null;
    }

    private void setCurrentTime() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public String format() {
        return isEmpty() ? "Undefined time" : format(hour, minute);
    }

    public static String format(int hour, int minute) {
        String hourPadding = "" + (hour < 10 ? 0 : "");
        String minutePadding = "" + (minute < 10 ? 0 : "");
        return hourPadding + hour + ":" + minutePadding + minute;
    }

    public String toString() {
        return format();
    }
}
