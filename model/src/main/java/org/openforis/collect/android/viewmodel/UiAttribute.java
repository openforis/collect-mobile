package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public abstract class UiAttribute extends UiNode {

    public UiAttribute(int id, boolean relevant, UiAttributeDefinition definition) {
        super(id, relevant, definition);
    }

    public UiAttributeDefinition getDefinition() {
        return (UiAttributeDefinition) super.getDefinition();
    }

    public boolean isCalculated() {
        return getDefinition().calculated;
    }

    public boolean isCalculatedOnlyOneTime() {
        return getDefinition().calculatedOnlyOneTime;
    }

    public abstract boolean isEmpty();

    public abstract String valueAsString();
}
