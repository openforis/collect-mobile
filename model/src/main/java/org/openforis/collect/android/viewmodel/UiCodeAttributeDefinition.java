package org.openforis.collect.android.viewmodel;

public class UiCodeAttributeDefinition extends UiAttributeDefinition {
    private final boolean valueShown;

    public UiCodeAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId, boolean calculated, String description, String prompt, boolean required, boolean valueShown) {
        super(id, name, label, keyOfDefinitionId, calculated, description, prompt, required);
        this.valueShown = valueShown;
    }

    public boolean isValueShown() {
        return valueShown;
    }
}
