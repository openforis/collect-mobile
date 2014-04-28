package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiAttributeCollectionDefinition extends Definition {
    public final Class<? extends UiAttribute> attributeType;
    public final Definition attributeDefinition;

    public UiAttributeCollectionDefinition(String id, String name, String label,
                                           Class<? extends UiAttribute> attributeType,
                                           Definition attributeDefinition) {
        super(id, name, label);
        this.attributeType = attributeType;
        this.attributeDefinition = attributeDefinition;
    }

    public boolean isOfAttributeType(Class<? extends UiAttribute> uiTextAttributeClass) {
        return uiTextAttributeClass.isAssignableFrom(attributeType);
    }
}
