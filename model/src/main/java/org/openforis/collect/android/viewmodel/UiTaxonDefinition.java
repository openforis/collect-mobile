package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonDefinition extends UiAttributeDefinition {
    public final String taxonomy;

    public UiTaxonDefinition(String id, String name, String label, Integer keyOfDefinitionId,
                             boolean calculated, boolean calculatedOnlyOneTime,
                             String taxonomy, String description, String prompt, String interviewLabel, boolean required) {
        super(id, name, label, keyOfDefinitionId, calculated, calculatedOnlyOneTime, description, prompt, interviewLabel, required);
        this.taxonomy = taxonomy;
    }
}
