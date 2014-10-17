package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

public class NodeAddedResult<T extends UiNode> {
    public final T nodeAdded;
    public final Map<UiNode, UiNodeChange> nodeChanges;

    public NodeAddedResult(T nodeAdded, Map<UiNode, UiNodeChange> nodeChanges) {
        this.nodeAdded = nodeAdded;
        this.nodeChanges = nodeChanges;
    }
}
