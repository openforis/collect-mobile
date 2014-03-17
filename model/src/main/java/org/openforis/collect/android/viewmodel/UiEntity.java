package org.openforis.collect.android.viewmodel;

import java.util.*;

/**
 * @author Daniel Wiell
 */
public class UiEntity extends UiInternalNode {
    private Set<Integer> keyAttributeIds = new HashSet<Integer>();
    private List<UiAttribute> keyAttributes = new ArrayList<UiAttribute>();

    public UiEntity(int id, Definition definition) {
        super(id, definition);
    }

    public List<UiAttribute> getKeyAttributes() {
        return Collections.unmodifiableList(keyAttributes);
    }

    public boolean isKeyAttribute(UiAttribute attribute) {
        return keyAttributeIds.contains(attribute.getId());
    }

    public void register(UiNode node) {
        super.register(node);
        if (node instanceof UiAttribute && node.getDefinition().isKeyOf(this)) {
            keyAttributes.add((UiAttribute) node);
            keyAttributeIds.add(node.getId());
        }
    }
}
