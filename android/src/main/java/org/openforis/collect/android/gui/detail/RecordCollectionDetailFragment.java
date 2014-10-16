package org.openforis.collect.android.gui.detail;

import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    protected void deleteNodes(Collection<Integer> nodeIds) {
        surveyService().deleteRecords(nodeIds);
    }
}
