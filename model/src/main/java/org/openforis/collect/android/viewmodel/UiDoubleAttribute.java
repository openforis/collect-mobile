package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiDoubleAttribute extends UiAttribute {
    private Double value; // TODO: Need unit and precision

    public UiDoubleAttribute(int id, Definition definition) {
        super(id, definition);
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

    public String toString() {
        return getLabel() + ": " + (value == null ? "Unspecified" : value);
    }
}
