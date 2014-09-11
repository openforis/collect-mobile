package org.openforis.collect.android.viewmodel;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
public abstract class UiAttribute extends UiNode {

    public UiAttribute(int id, boolean relevant, Definition definition) {
        super(id, relevant, definition);
    }

    public abstract boolean isEmpty();

    public abstract String valueAsString();
}
