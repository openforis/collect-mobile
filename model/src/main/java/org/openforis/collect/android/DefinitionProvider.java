package org.openforis.collect.android;

import org.openforis.collect.android.viewmodel.Definition;

/**
 * @author Daniel Wiell
 */
public interface DefinitionProvider {
    Definition getById(String definitionId);
}
