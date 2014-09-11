package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiDoubleAttribute extends UiAttribute {
    private Double value; // TODO: Need unit and precision

    public UiDoubleAttribute(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public synchronized Double getValue() {
        return value;
    }

    public synchronized void setValue(Double value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public String valueAsString() {
        return value == null ? null : value.toString();
    }

    public String toString() {
        return getLabel() + ": " + (value == null ? "Unspecified" : value);
    }
}
