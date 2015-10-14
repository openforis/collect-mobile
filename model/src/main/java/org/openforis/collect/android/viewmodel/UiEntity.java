package org.openforis.collect.android.viewmodel;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class UiEntity extends UiInternalNode {
    private Set<Integer> keyAttributeIds = new HashSet<Integer>();
    private List<UiAttribute> keyAttributes = new ArrayList<UiAttribute>();

    public UiEntity(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public List<UiAttribute> getKeyAttributes() {
        return Collections.unmodifiableList(keyAttributes);
    }

    public boolean isKeyAttribute(UiAttribute attribute) {
        return keyAttributeIds.contains(attribute.getId());
    }

    public void register(UiNode node) {
        super.register(node);
        if (node instanceof UiAttribute
                && node.getDefinition().isKeyOf(this)
                && !keyAttributeIds.contains(node.getId())) {
            keyAttributes.add((UiAttribute) node);
            keyAttributeIds.add(node.getId());
        }
    }

    public void unregister(UiNode node) {
        super.unregister(node);
        if (node instanceof UiAttribute && node.getDefinition().isKeyOf(this)) {
            keyAttributes.remove(node);
            keyAttributeIds.remove(node.getId());
        }
    }
}
