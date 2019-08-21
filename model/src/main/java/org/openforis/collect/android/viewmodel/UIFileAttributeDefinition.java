package org.openforis.collect.android.viewmodel;

import org.openforis.collect.metamodel.CollectAnnotations.FileType;

public class UIFileAttributeDefinition extends UiAttributeDefinition {

    private final FileType type;

    public UIFileAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                     boolean calculated, boolean calculatedOnlyOneTime, boolean hidden,
                                     String description, String prompt, String interviewLabel,
                                     boolean required, FileType type) {
        super(id, name, label, keyOfDefinitionId, calculated, calculatedOnlyOneTime, hidden, description, prompt, interviewLabel, required);
        this.type = type;
    }

    public FileType getType() {
        return type;
    }
}
