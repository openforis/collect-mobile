package org.openforis.collect.android.viewmodel;

public class UiCodeAttributeDefinition extends UiAttributeDefinition {
    private final boolean valueShown;
    private final boolean enumerator;

    public UiCodeAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                     boolean calculated, String description, String prompt, boolean required,
                                     boolean valueShown, boolean enumerator) {
        super(id, name, label, keyOfDefinitionId, calculated, description, prompt, required);
        this.valueShown = valueShown;
        this.enumerator = enumerator;
    }

    public boolean isValueShown() {
        return valueShown;
    }

    public boolean isEnumerator() {
        return enumerator;
    }
}
