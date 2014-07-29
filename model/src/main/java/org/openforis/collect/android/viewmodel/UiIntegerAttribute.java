package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiIntegerAttribute extends UiAttribute {
    private Integer value; // TODO: Need unit and precision

    public UiIntegerAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public synchronized Integer getValue() {
        return value;
    }

    public synchronized void setValue(Integer value) {
        this.value = value;
    }

    public String valueAsString() {
        return value == null ? null : value.toString();
    }

    public boolean isEmpty() {
        return value == null;
    }

    public String toString() {
        return getLabel() + ": " + (value == null ? "Unspecified" : value);
    }
}
