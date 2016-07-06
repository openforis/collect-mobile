package org.openforis.collect.android.viewmodel;

import com.google.common.base.Objects;
import org.apache.commons.lang3.Validate;
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

    public int hashCode() {
        return Objects.hashCode(value);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UiCode other = (UiCode) obj;
        return Objects.equal(this.value, other.value);
    }
}
