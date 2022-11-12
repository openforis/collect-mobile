package org.openforis.collect.android.viewmodel;

import org.openforis.collect.metamodel.CollectAnnotations.FileType;

public class UIFileAttributeDefinition extends UiAttributeDefinition {

    private final FileType type;
    private final Integer maxSize;

    public UIFileAttributeDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                     boolean calculated, boolean calculatedOnlyOneTime, boolean hidden,
                                     String description, String prompt, String interviewLabel,
                                     boolean required, FileType type, Integer maxSize) {
        super(id, name, label, keyOfDefinitionId, calculated, calculatedOnlyOneTime, hidden, description, prompt, interviewLabel, required);
        this.type = type;
        this.maxSize = maxSize;
    }

    public FileType getType() {
        return type;
    }

    public Integer getMaxSize() {
        return maxSize;
    }
}
