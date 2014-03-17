package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiDoubleRangeAttribute extends UiAttribute {
    private Double from; // TODO: Need unit and precision
    private Double to;

    public UiDoubleRangeAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public synchronized Double getFrom() {
        return from;
    }

    public synchronized void setFrom(Double from) {
        this.from = from;
    }

    public synchronized Double getTo() {
        return to;
    }

    public synchronized void setTo(Double to) {
        this.to = to;
    }

    public String toString() {
        return getLabel() + ": " + (from == null ? "Unspecified" : from) + "-" + (to == null ? "Unspecified" : to);
    }
}
