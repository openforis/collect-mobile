package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiCodeAttribute extends UiAttribute {
    private UiCode code;

    public UiCodeAttribute(int id, Definition definition) {
        super(id, definition);
    }

    public synchronized void setCode(UiCode code) {
        this.code = code;
    }

    public synchronized UiCode getCode() {
        return code;
    }

    public String toString() {
        return getLabel() + ": " + (code == null ? "Unspecified" : code); // TODO: How to translate "Unspecified"? Put this logic in GUI
    }
}
