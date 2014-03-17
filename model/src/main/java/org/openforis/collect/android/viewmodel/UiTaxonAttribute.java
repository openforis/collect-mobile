package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonAttribute extends UiAttribute {
    private UiTaxon taxon;

    public UiTaxonAttribute(int id, UiTaxonDefinition definition) {
        super(id, definition);
    }

    public synchronized UiTaxon getTaxon() {
        return taxon;
    }

    public synchronized void setTaxon(UiTaxon taxon) {
        this.taxon = taxon;
    }

    public UiTaxonDefinition getDefinition() {
        return (UiTaxonDefinition) super.getDefinition();
    }

    public boolean isEmpty() {
        return taxon == null;
    }

    public String toString() {
        return getLabel() + ": " + (taxon == null ? "Unspecified" : taxon);
    }
}
