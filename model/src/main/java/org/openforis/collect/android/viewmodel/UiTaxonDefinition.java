package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonDefinition extends Definition {
    private final String taxonomy;

    public UiTaxonDefinition(String id, String name, String label, Integer keyOfDefinitionId, String taxonomy, String description, String prompt) {
        super(id, name, label, keyOfDefinitionId, description, prompt);
        this.taxonomy = taxonomy;
    }

    public String getTaxonomy() {
        return taxonomy;
    }
}
