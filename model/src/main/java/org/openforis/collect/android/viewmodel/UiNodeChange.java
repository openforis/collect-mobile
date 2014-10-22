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
    public Set<UiValidationError> validationErrors = new HashSet<UiValidationError>();

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("statusChange", statusChange)
                .add("relevanceChange", relevanceChange)
                .add("validationErrors", validationErrors)
                .toString();
    }
}
