package org.openforis.collect.android.viewmodel;

/**
 * @author S. Ricci
 */
public class UiEntityCollectionDefinition extends Definition {

    private final boolean enumerated;
    private final Integer fixedMinCount;
    private final Integer fixedMaxCount;

    public UiEntityCollectionDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                                        String description, String prompt, boolean required, boolean enumerated,
                                        Integer fixedMinCount, Integer fixedMaxCount) {
        super(id, name, label, keyOfDefinitionId, description, prompt, null, required);
        this.enumerated = enumerated;
        this.fixedMinCount = fixedMinCount;
        this.fixedMaxCount = fixedMaxCount;
    }

    public boolean isEnumerated() {
        return enumerated;
    }

    public Integer getFixedMinCount() {
        return fixedMinCount;
    }

    public Integer getFixedMaxCount() {
        return fixedMaxCount;
    }
}
