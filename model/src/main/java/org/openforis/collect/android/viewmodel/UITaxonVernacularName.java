package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UITaxonVernacularName {

    private final String name;

    private final String languageCode;

    public UITaxonVernacularName(String name, String languageCode) {
        this.name = name;
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UITaxonVernacularName that = (UITaxonVernacularName) o;

        return new EqualsBuilder().append(languageCode, that.languageCode).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(languageCode).append(name).toHashCode();
    }
}
