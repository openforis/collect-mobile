package org.openforis.collect.android.viewmodelmanager;

import org.openforis.collect.android.viewmodel.UiNode;

public class StatusChange {
    public final String status;
    public final boolean relevant;

    public StatusChange(UiNode node) {
        this.status = node.getStatus().name();
        this.relevant = node.isRelevant();
    }
}
