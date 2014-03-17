package org.openforis.collect.android.viewmodelmanager;

import org.openforis.collect.android.viewmodel.UiTaxon;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public interface TaxonService {
    List<UiTaxon> find(String query, String taxonomy, int maxResults);
}
