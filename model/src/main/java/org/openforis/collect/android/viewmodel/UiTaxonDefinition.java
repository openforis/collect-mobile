package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonDefinition extends Definition {
    private final String taxonomy;

    public UiTaxonDefinition(String id, String name, String label, Integer keyOfDefinitionId, String taxonomy) {
        super(id, name, label, keyOfDefinitionId);
        this.taxonomy = taxonomy;
    }

    public String getTaxonomy() {
        return taxonomy;
    }
}
