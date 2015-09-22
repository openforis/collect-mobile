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

    public UiCode(String value, String label) {
        this(value, label, null);
    }


    public UiCode(String value, String label, String description) {
        Validate.notEmpty(value, "value is required");
        this.value = value;
        this.label = StringUtils.normalizeWhiteSpace(label);
        this.description = description;
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

    public String toString() {
        if (label == null)
            return value;
        return label + (label.equals(value) ? "" : " (" + value + ")");
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
