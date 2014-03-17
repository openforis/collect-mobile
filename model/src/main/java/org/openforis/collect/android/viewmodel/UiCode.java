package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.Validate;

/**
 * @author Daniel Wiell
 */
public class UiCode {
    private final String value;
    private final String label;

    public UiCode(String value, String label) {
        Validate.notEmpty(value, "value is required");
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        if (label == null)
            return value;
        return label + " (" + value + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiCode uiCode = (UiCode) o;

        return !(value != null ? !value.equals(uiCode.value) : uiCode.value != null);

    }

    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
