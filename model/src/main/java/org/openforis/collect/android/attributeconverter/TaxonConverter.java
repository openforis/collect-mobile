package org.openforis.collect.android.attributeconverter;

import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.android.viewmodelmanager.NodeDto;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Value;

/**
 * @author Daniel Wiell
 */
class TaxonConverter extends AttributeConverter<TaxonAttribute, UiTaxonAttribute> {
    public UiTaxonAttribute uiAttribute(UiAttributeDefinition definition, TaxonAttribute attribute) {
        UiTaxonAttribute uiAttribute = new UiTaxonAttribute(attribute.getId(), isRelevant(attribute), (UiTaxonDefinition) definition);
        updateUiAttributeValue(attribute, uiAttribute);
        return uiAttribute;
    }

    protected void updateUiAttributeValue(TaxonAttribute attribute, UiTaxonAttribute uiAttribute) {
        String code = attribute.getCodeField().getValue();
        String scientificName = attribute.getScientificName();
        if (code != null)
            uiAttribute.setTaxon(new UiTaxon(code, scientificName));
        else
            uiAttribute.setTaxon(null);
    }

    protected UiTaxonAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiTaxonAttribute uiAttribute = new UiTaxonAttribute(nodeDto.id, nodeDto.relevant, (UiTaxonDefinition) definition);
        if (nodeDto.taxonCode != null)
            uiAttribute.setTaxon(new UiTaxon(nodeDto.taxonCode, nodeDto.taxonScientificName));
        return uiAttribute;
    }

    protected NodeDto dto(UiTaxonAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        UiTaxon taxon = uiAttribute.getTaxon();
        if (taxon != null) {
            dto.taxonCode = taxon.getCode();
            dto.taxonScientificName = taxon.getScientificName();
        }
        return dto;
    }

    public Value value(UiTaxonAttribute uiAttribute) {
        UiTaxon taxon = uiAttribute.getTaxon();
        return taxon == null
                ? new TaxonOccurrence((String) null, null)
                : new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
    }

    protected TaxonAttribute attribute(UiTaxonAttribute uiAttribute, NodeDefinition definition) {
        TaxonAttribute a = new TaxonAttribute((TaxonAttributeDefinition) definition);
        if (!uiAttribute.isCalculated())
            a.setValue((TaxonOccurrence) value(uiAttribute));
        return a;
    }
}
