package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiBooleanAttribute extends UiAttribute {
    private Boolean value;
    // TODO: Can be a multi-line or not

    public UiBooleanAttribute(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public synchronized Boolean getValue() {
        return value;
    }

    public synchronized void setValue(Boolean value) {
        this.value = value;
    }

    public String valueAsString() {
        return value == null ? null : value.toString();
    }

    public boolean isEmpty() {
        return value == null;
    }

    public String toString() {
        return getLabel() + ": " + value;
    }
}
