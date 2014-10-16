package org.openforis.collect.android.viewmodel;

import com.google.common.base.Objects;
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

    public int hashCode() {
        return Objects.hashCode(code);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UiTaxon other = (UiTaxon) obj;
        return Objects.equal(this.code, other.code);
    }
}
