package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiDoubleRangeAttribute extends UiAttribute {
    private Double from; // TODO: Need unit and precision
    private Double to;

    public UiDoubleRangeAttribute(int id, boolean relevant, UiAttributeDefinition definition) {
        super(id, relevant, definition);
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

    public String valueAsString() {
        return isEmpty() ? null : (from + "-" + to);
    }

    public boolean isEmpty() {
        return from == null || to == null;
    }

    public String toString() {
        return getLabel() + ": " + (from == null ? "Unspecified" : from) + "-" + (to == null ? "Unspecified" : to);
    }
}
