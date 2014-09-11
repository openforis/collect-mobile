package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiCodeAttribute extends UiAttribute {
    private UiCode code;
    private String qualifier;

    public UiCodeAttribute(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public synchronized void setCode(UiCode code) {
        this.code = code;
    }

    public synchronized UiCode getCode() {
        return code;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String valueAsString() {
        return code == null ? null : (code.toString() + (qualifier == null ? "" : ", " + qualifier));
    }

    public boolean isEmpty() {
        return code == null;
    }

    public String toString() {
        return getLabel() + ": " + (code == null ? "Unspecified" : code);
    }
}
