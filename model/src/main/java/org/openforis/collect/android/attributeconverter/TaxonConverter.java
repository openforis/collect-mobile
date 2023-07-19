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
        if (code != null) {
            UITaxonVernacularName vernacularName = null;
            String vernacularNameStr = attribute.getVernacularName();
            if (vernacularNameStr != null) {
                String langCode = attribute.getLanguageCode();
                vernacularName = new UITaxonVernacularName(vernacularNameStr, langCode);
            }
            uiAttribute.setTaxon(new UiTaxon(code, scientificName, vernacularName));
        } else {
            uiAttribute.setTaxon(null);
        }
    }

    protected UiTaxonAttribute uiAttribute(NodeDto nodeDto, UiAttributeDefinition definition) {
        UiTaxonAttribute uiAttribute = new UiTaxonAttribute(nodeDto.id, nodeDto.relevant, (UiTaxonDefinition) definition);
        if (nodeDto.taxonCode != null) {
            String vernacularNameStr = nodeDto.taxonVernacularName;
            UITaxonVernacularName vernacularName = vernacularNameStr == null ? null : new UITaxonVernacularName(vernacularNameStr, nodeDto.taxonVernacularNameLangCode);
            uiAttribute.setTaxon(new UiTaxon(nodeDto.taxonCode, nodeDto.taxonScientificName, vernacularName));
        }
        return uiAttribute;
    }

    protected NodeDto dto(UiTaxonAttribute uiAttribute) {
        NodeDto dto = createDto(uiAttribute);
        UiTaxon taxon = uiAttribute.getTaxon();
        if (taxon != null) {
            UITaxonVernacularName vernacularName = taxon.getVernacularName();
            dto.taxonCode = taxon.getCode();
            dto.taxonScientificName = taxon.getScientificName();
            dto.taxonVernacularName = vernacularName == null ? null : vernacularName.getName();
            dto.taxonVernacularNameLangCode = vernacularName == null ? null : vernacularName.getLanguageCode();
        }
        return dto;
    }

    public Value value(UiTaxonAttribute uiAttribute) {
        UiTaxon taxon = uiAttribute.getTaxon();
        if (taxon == null) {
            return new TaxonOccurrence((String) null, null);
        }
        TaxonOccurrence taxonOccurrence = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
        UITaxonVernacularName vernacularName = taxon.getVernacularName();
        if (vernacularName != null) {
            taxonOccurrence.setVernacularName(vernacularName.getName());
            taxonOccurrence.setLanguageCode(vernacularName.getLanguageCode());
        }
        return taxonOccurrence;
    }

    protected TaxonAttribute attribute(UiTaxonAttribute uiAttribute, NodeDefinition definition) {
        TaxonAttribute a = new TaxonAttribute((TaxonAttributeDefinition) definition);
        if (!uiAttribute.isCalculated() || uiAttribute.isCalculatedOnlyOneTime())
            a.setValue((TaxonOccurrence) value(uiAttribute));
        return a;
    }
}
