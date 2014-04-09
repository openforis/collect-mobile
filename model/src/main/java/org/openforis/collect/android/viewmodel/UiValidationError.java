package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiValidationError {
    private final String message;
    private final Level level;
    private final UiAttribute attribute;

    public UiValidationError(String message, Level level, UiAttribute attribute) {
        this.message = message;
        this.level = level;
        this.attribute = attribute;
    }

    public Level getLevel() {
        return level;
    }

    public UiAttribute getAttribute() {
        return attribute;
    }

    public String toString() {
        return message;
    }
    public enum Level {
        WARNING, ERROR
    }
}
