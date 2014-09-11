package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiTaxonAttribute extends UiAttribute {
    private UiTaxon taxon;

    public UiTaxonAttribute(int id, boolean relevant, UiTaxonDefinition definition) {
        super(id, relevant, definition);
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

    public String valueAsString() {
        return taxon == null ? null : taxon.toString();
    }

    public boolean isEmpty() {
        return taxon == null;
    }

    public String toString() {
        return getLabel() + ": " + (taxon == null ? "Unspecified" : taxon);
    }
}
