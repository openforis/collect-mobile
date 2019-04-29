package org.openforis.collect.android.viewmodel;

public class UiAttributeDefinition extends Definition {
    public final boolean calculated;
    public final boolean calculatedOnlyOneTime;

    public UiAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                 boolean calculated, boolean calculatedOnlyOneTime, String description, String prompt, boolean required) {
        super(id, name, label, keyOfDefinitionId, description, prompt, required);
        this.calculated = calculated;
        this.calculatedOnlyOneTime = calculatedOnlyOneTime;
    }

    public UiAttributeDefinition(String id, String name, String label, boolean required) {
        super(id, name, label, required);
        calculated = false;
        calculatedOnlyOneTime = false;
    }
}
