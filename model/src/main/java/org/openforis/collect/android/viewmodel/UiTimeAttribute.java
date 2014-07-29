package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTimeAttribute extends UiAttribute {
    private Integer hour;
    private Integer minute;

    public UiTimeAttribute(int id, Definition definition) {
        super(id, definition);
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

    public String valueAsString() {
        return isEmpty() ? null : format();
    }

    public boolean isEmpty() {
        return hour == null || minute == null;
    }

    public String format() {
        return isEmpty() ? "" : format(hour, minute);
    }

    public static String format(int hour, int minute) {
        String hourPadding = "" + (hour < 10 ? 0 : "");
        String minutePadding = "" + (minute < 10 ? 0 : "");
        return hourPadding + hour + ":" + minutePadding + minute;
    }

    public String toString() {
        return isEmpty() ? "Undefined time" : format();
    }
}
