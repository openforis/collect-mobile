package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiValidationError {
    private final Level level;

    public UiValidationError(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public enum Level {
        WARNING, ERROR
    }
}
