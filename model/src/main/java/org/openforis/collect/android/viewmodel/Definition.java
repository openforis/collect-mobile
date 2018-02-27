package org.openforis.collect.android.viewmodel;

import org.openforis.idm.metamodel.NodeDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class Definition {
    public final String id;
    public final String name;
    public final String label;
    public final Integer keyOfDefinitionId;
    public final String description;
    public final String prompt;
    public final boolean required;
    public Set<Definition> relevanceSources = new HashSet<Definition>();

    public Definition(String id, String name, String label, boolean required) {
        this(id, name, label, null, null, null, required);
    }

    public Definition(String id, String name, String label, Integer keyOfDefinitionId, String description,
                      String prompt, boolean required) {
        this.id = id;
        this.name = name;
        this.label = label == null ? name : label; // Use the name as label if not specified
        this.keyOfDefinitionId = keyOfDefinitionId;
        this.description = description;
        this.prompt = prompt;
        this.required = required;
    }

    public boolean isKeyOf(UiNode uiNode) {
        return keyOfDefinitionId != null && Integer.valueOf(uiNode.getDefinition().id).equals(keyOfDefinitionId);
    }

    public String toString() {
        return name;
    }
}
