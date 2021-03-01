package org.openforis.collect.android.viewmodel;

import org.openforis.collect.metamodel.CollectAnnotations.TextInput;

/**
 * @author Daniel Wiell
 */
public class UiTextAttributeDefinition extends UiAttributeDefinition {
    public final TextInput inputType;
    private boolean autoUppercase;

    public UiTextAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                     boolean calculated, boolean calculatedOnlyOneTime, boolean hidden,
                                     TextInput inputType,
                                     String description, String prompt, String interviewLabel,
                                     boolean required) {
        super(id, name, label, keyOfDefinitionId, calculated, calculatedOnlyOneTime, hidden, description, prompt, interviewLabel, required);
        this.inputType = inputType;
    }

    public TextInput getInputType() {
        return inputType;
    }

    public boolean isAutoUppercase() {
        return autoUppercase;
    }

    public void setAutoUppercase(boolean autoUppercase) {
        this.autoUppercase = autoUppercase;
    }
}
