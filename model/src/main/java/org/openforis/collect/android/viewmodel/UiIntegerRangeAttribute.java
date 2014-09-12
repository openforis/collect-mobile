package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiIntegerRangeAttribute extends UiAttribute {
    private Integer from; // TODO: Need unit and precision
    private Integer to;

    public UiIntegerRangeAttribute(int id, boolean relevant, UiAttributeDefinition definition) {
        super(id, relevant, definition);
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
