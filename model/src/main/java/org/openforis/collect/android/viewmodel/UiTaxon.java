package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Daniel Wiell
 */
public class UiTaxon {
    private final String code;
    private final String scientificName;
    private final UITaxonVernacularName vernacularName;

    public UiTaxon(String code, String scientificName) {
        this(code, scientificName, null);
    }

    public UiTaxon(String code, String scientificName, UITaxonVernacularName vernacularName) {
        Validate.notEmpty(code, "code is required");
        Validate.notEmpty(scientificName, "scientificName is required");
        this.code = code;
        this.scientificName = scientificName;
        this.vernacularName = vernacularName;
    }

    public String getCode() {
        return code;
    }

    public String getScientificName() {
        return scientificName;
    }

    public UITaxonVernacularName getVernacularName() { return vernacularName; }

    public String toString() {
        return scientificName + " (" + code + ")";
    }

    public String toStringFull() {
        return this + (vernacularName == null ? "" : " [" + vernacularName.getName() + "]");
    }

    public String toStringSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(scientificName);
        if (vernacularName != null) {
            sb.append(" [");
            sb.append(vernacularName.getName());
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UiTaxon uiTaxon = (UiTaxon) o;

        return new EqualsBuilder()
                .append(code, uiTaxon.code)
                .append(vernacularName, uiTaxon.vernacularName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).append(vernacularName).toHashCode();
    }
}
