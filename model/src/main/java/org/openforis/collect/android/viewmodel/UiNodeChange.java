package org.openforis.collect.android.viewmodel;

import com.google.common.base.MoreObjects;

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
        return MoreObjects.toStringHelper(this)
                .add("statusChange", statusChange)
                .add("valueChange", valueChange)
                .add("relevanceChange", relevanceChange)
                .add("validationErrors", validationErrors)
                .toString();
    }
}
