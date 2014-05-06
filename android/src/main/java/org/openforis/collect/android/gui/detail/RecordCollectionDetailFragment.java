package org.openforis.collect.android.gui.detail;

import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

/**
 * @author Daniel Wiell
 */
public class RecordCollectionDetailFragment extends AbstractNodeCollectionDetailFragment<UiRecordCollection> {

    protected UiInternalNode addNode() {
        return surveyService().addRecord(node().getName());
    }

    protected UiInternalNode getSelectedNode(int position, UiRecordCollection recordCollection) {
        UiNode recordPlaceholder = recordCollection.getChildAt(position);
        return surveyService().selectRecord(recordPlaceholder.getId());
    }

    protected void removeNode(UiNode node) {
        surveyService().removeRecord(node.getId());
    }
}
