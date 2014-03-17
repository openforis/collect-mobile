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

    public static class RequiredAttributeMissing extends UiValidationError {
        private final UiAttribute attribute;

        public RequiredAttributeMissing(Level level, UiAttribute attribute) {
            super(level);
            this.attribute = attribute;
        }

        public UiAttribute getAttribute() {
            return attribute;
        }

        public String toString() {
            return attribute.getLabel() + " is required";
        }
    }

    public enum Level {
        WARNING, ERROR
    }
}
