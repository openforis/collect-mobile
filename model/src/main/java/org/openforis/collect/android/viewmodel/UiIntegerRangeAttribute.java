package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiIntegerRangeAttribute extends UiAttribute {
    private Integer from; // TODO: Need unit and precision
    private Integer to;

    public UiIntegerRangeAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public synchronized Integer getFrom() {
        return from;
    }

    public synchronized void setFrom(Integer from) {
        this.from = from;
    }

    public synchronized Integer getTo() {
        return to;
    }

    public synchronized void setTo(Integer to) {
        this.to = to;
    }

    public String toString() {
        return getLabel() + ": " + (from == null ? "Unspecified" : from) + "-" + (to == null ? "Unspecified" : to);
    }
}
