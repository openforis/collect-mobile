package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiAttributeCollectionDefinition extends Definition {
    public final Class<? extends UiAttribute> attributeType;
    public final UiAttributeDefinition attributeDefinition;

    public UiAttributeCollectionDefinition(String id, String name, String label,
                                           Class<? extends UiAttribute> attributeType,
                                           UiAttributeDefinition attributeDefinition,
                                           boolean required) {
        super(id, name, label, null, attributeDefinition.description, null, null, required);
        this.attributeType = attributeType;
        this.attributeDefinition = attributeDefinition;
    }

    public boolean isOfAttributeType(Class<? extends UiAttribute> uiAttributeClass) {
        return uiAttributeClass.isAssignableFrom(attributeType);
    }
}
