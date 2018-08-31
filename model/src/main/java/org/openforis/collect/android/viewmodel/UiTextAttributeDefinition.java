package org.openforis.collect.android.viewmodel;

import org.openforis.collect.metamodel.CollectAnnotations.TextInput;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author Daniel Wiell
 */
public class UiTextAttributeDefinition extends UiAttributeDefinition {
    public final TextInput inputType;

    public UiTextAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId, boolean calculated,
                                     TextInput inputType, String description, String prompt, boolean required) {
        super(id, name, label, keyOfDefinitionId, calculated, description, prompt, required);
        this.inputType = inputType;
    }

    public TextInput getInputType() {
        return inputType;
    }
}
