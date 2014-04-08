package org.openforis.collect.android.viewmodel;

/**
* @author Daniel Wiell
*/
public class AttributeValidationError extends UiValidationError {
    private final UiAttribute attribute;
    private final String message;

    public AttributeValidationError(String message, Level level, UiAttribute attribute) {
        super(level);
        this.message = message;
        this.attribute = attribute;
    }

    public UiAttribute getAttribute() {
        return attribute;
    }

    public String toString() {
        return message;
    }
}
