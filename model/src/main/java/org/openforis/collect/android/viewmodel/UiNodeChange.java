package org.openforis.collect.android.viewmodel;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class UiNodeChange {
    public boolean statusChange;
    public boolean relevanceChange;
    public boolean valueChange;
    public Set<UiValidationError> validationErrors = new HashSet<UiValidationError>();

    public static UiNodeChange statusChanged() {
        UiNodeChange change = new UiNodeChange();
        change.statusChange = true;
        return change;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("statusChange", statusChange)
                .append("valueChange", valueChange)
                .append("relevanceChange", relevanceChange)
                .append("validationErrors", validationErrors)
                .toString();
    }
}
