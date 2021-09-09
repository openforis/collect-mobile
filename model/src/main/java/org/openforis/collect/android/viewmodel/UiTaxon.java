package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UiTaxon uiTaxon = (UiTaxon) o;

        return new EqualsBuilder().append(code, uiTaxon.code).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).toHashCode();
    }
}
