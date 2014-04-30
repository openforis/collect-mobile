package org.openforis.collect.android.viewmodel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class UiAttributeChange {
    public boolean statusChange;
    public boolean relevanceChange;
    public Set<UiValidationError> validationErrors = new HashSet<UiValidationError>();
}
