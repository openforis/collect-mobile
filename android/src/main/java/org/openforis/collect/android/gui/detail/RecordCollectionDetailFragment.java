package org.openforis.collect.android.gui.detail;

import android.view.Menu;
import android.view.MenuInflater;
import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.Collection;

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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_entity_table);
    }

    protected void deleteNodes(Collection<Integer> nodeIds) {
        surveyService().deleteRecords(nodeIds);
    }
}
