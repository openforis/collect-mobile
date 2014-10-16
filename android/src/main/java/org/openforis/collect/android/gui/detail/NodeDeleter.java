package org.openforis.collect.android.gui.detail;

import org.openforis.collect.android.viewmodel.UiNode;

import java.util.Collection;

public interface NodeDeleter {
    void delete(Collection<UiNode> nodes);
}
