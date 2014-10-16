package org.openforis.collect.android.gui.detail;

import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class EntityCollectionDetailFragment extends AbstractNodeCollectionDetailFragment<UiEntityCollection> {
    protected UiInternalNode addNode() {
        return surveyService().addEntity();
    }

    protected UiInternalNode getSelectedNode(int position, UiEntityCollection entityCollection) {
        return (UiInternalNode) entityCollection.getChildAt(position);
    }

    protected void deleteNodes(Collection<Integer> nodeIds) {
        surveyService().deleteEntities(nodeIds);
    }
}
