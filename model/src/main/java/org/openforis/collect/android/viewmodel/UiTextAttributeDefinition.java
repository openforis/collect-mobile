package org.openforis.collect.android.viewmodel;

import org.openforis.collect.metamodel.CollectAnnotations.TextInput;

/**
 * @author Daniel Wiell
 */
public class UiTextAttributeDefinition extends UiAttributeDefinition {
    public final TextInput inputType;

    public UiTextAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                     boolean calculated, boolean calculatedOnlyOneTime,
                                     TextInput inputType,
                                     String description, String prompt, String interviewLabel,
                                     boolean required) {
        super(id, name, label, keyOfDefinitionId, calculated, calculatedOnlyOneTime, description, prompt, interviewLabel, required);
        this.inputType = inputType;
    }

    public TextInput getInputType() {
        return inputType;
    }
}
