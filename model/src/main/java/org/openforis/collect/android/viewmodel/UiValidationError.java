package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiValidationError {
    private final String message;
    private final Level level;
    private final UiNode node;

    public UiValidationError(String message, Level level, UiNode node) {
        this.message = message;
        this.level = level;
        this.node = node;
    }

    public Level getLevel() {
        return level;
    }

    public UiNode getNode() {
        return node;
    }

    public String toString() {
        return message;
    }

    public enum Level {
        WARNING, ERROR
    }
}
