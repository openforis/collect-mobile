package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class Definition {
    public final String id;
    public final String name; // TODO: Needed?
    public final String label;
    public final Integer keyOfDefinitionId;

    public Definition(String id, String name, String label) {
        this(id, name, label, null);
    }

    public Definition(String id, String name, String label, Integer keyOfDefinitionId) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.keyOfDefinitionId = keyOfDefinitionId;
    }

    public boolean isKeyOf(UiNode uiNode) {
        return keyOfDefinitionId != null && Integer.valueOf(uiNode.getDefinition().id).equals(keyOfDefinitionId);
    }

    public String toString() {
        return name;
    }
}
