package org.openforis.collect.android.viewmodelmanager;

import org.openforis.collect.android.viewmodel.UiTaxon;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public interface TaxonService {
    List<UiTaxon> find(String query, String taxonomy, int maxResults);

    Map<String, String> commonNameByLanguage(String taxonCode, String taxonomy);
}
