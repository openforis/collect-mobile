package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.Validate;

/**
 * @author Daniel Wiell
 */
public class UiTaxon {
    private final String code;
    private final String scientificName;

    public UiTaxon(String code, String scientificName) {
        Validate.notEmpty(code, "code is required");
        Validate.notEmpty(scientificName, "scientificName is required");
        this.code = code;
        this.scientificName = scientificName;
    }

    public String getCode() {
        return code;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String toString() {
        return scientificName + " (" + code + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiTaxon uiTaxon = (UiTaxon) o;
        return !(code != null ? !code.equals(uiTaxon.code) : uiTaxon.code != null);
    }

    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
