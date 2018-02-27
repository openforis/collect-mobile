package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonDefinition extends UiAttributeDefinition {
    public final String taxonomy;

    public UiTaxonDefinition(String id, String name, String label, Integer keyOfDefinitionId, boolean calculated,
                             String taxonomy, String description, String prompt, boolean required) {
        super(id, name, label, keyOfDefinitionId, calculated, description, prompt, required);
        this.taxonomy = taxonomy;
    }
}
