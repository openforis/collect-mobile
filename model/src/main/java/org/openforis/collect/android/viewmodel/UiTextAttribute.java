package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTextAttribute extends UiAttribute {
    private String text;
    // TODO: Can be a multi-line or not

    public UiTextAttribute(int id, boolean relevant, UiAttributeDefinition definition) {
        super(id, relevant, definition);
    }

    public synchronized String getText() {
        return text;
    }

    public synchronized void setText(String text) {
        this.text = text;
    }

    public String valueAsString() {
        return text;
    }

    public boolean isEmpty() {
        return text == null;
    }

    public String toString() {
        return getLabel() + ": " + (text == null ? "Unspecified" : text);
    }
}
