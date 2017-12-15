package org.openforis.collect.android.viewmodel;

/**
 * @author S. Ricci
 */

public class UiEntityCollectionDefinition extends Definition {

    private final boolean enumerated;

    public UiEntityCollectionDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                        String description, String prompt, boolean required, boolean enumerated) {
        super(id, name, label, keyOfDefinitionId, description, prompt, required);
        this.enumerated = enumerated;
    }

    public boolean isEnumerated() {
        return enumerated;
    }
}
