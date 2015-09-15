package org.openforis.collect.android.viewmodel;

import com.google.common.base.Objects;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class UiTaxon {
    private final String code;
    private final String scientificName;
    private final List<String> commonNames;

    public UiTaxon(String code, String scientificName) {
        this(code, scientificName, Collections.<String>emptyList());
    }

    public UiTaxon(String code, String scientificName, List<String> commonNames) {
        Validate.notEmpty(code, "code is required");
        Validate.notEmpty(scientificName, "scientificName is required");
        this.code = code;
        this.scientificName = scientificName;
        this.commonNames = commonNames;
    }

    public String getCode() {
        return code;
    }

    public String getScientificName() {
        return scientificName;
    }

    public List<String> getCommonNames() { return commonNames; }

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
