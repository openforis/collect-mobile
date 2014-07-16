package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class Definition {
    public final String id;
    public final String name; // TODO: Needed?
    public String label; // TODO: Ugly to have this mutable
    public final Integer keyOfDefinitionId;
    public final String description;
    public final String prompt;
    public final boolean required;


    public Definition(String id, String name, String label, boolean required) {
        this(id, name, label, null, null, null, required);
    }

    public Definition(String id, String name, String label, Integer keyOfDefinitionId, String description, String prompt, boolean required) {
        this.id = id;
        this.name = name;
        this.label = label;
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
