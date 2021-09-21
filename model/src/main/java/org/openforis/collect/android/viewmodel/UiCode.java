package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openforis.collect.android.util.StringUtils;

/**
 * @author Daniel Wiell
 */
public class UiCode {
    private final String value;
    private final String label;
    private final String description;
    private final boolean valueShown;

    public UiCode(String value, String label) {
        this(value, label, null, true);
    }

    public UiCode(String value, String label, String description, boolean valueShown) {
        Validate.notEmpty(value, "value is required");
        this.value = value;
        this.label = StringUtils.normalizeWhiteSpace(label);
        this.description = description;
        this.valueShown = valueShown;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isValueShown() {
        return valueShown;
    }

    public String toString() {
        if (label == null)
            return value;
        return label + (label.equals(value) || !valueShown ? "" : " (" + value + ")");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UiCode uiCode = (UiCode) o;

        return new EqualsBuilder().append(value, uiCode.value).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(value).toHashCode();
    }
}
