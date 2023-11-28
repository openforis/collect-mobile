package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.list.NodeListAdapter;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiInternalNode;

/**
 * @author Daniel Wiell
 */
public class EntityCollectionDetailFragment extends AbstractNodeCollectionDetailFragment<UiEntityCollection> {

    private View validationMessagesContainer;
    private TextView validationMessagesTextView;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.validationMessagesContainer = view.findViewById(R.id.validation_error_container);
        this.validationMessagesTextView = view.findViewById(R.id.validation_error_messages);
    }

    @Override
    protected void updateEditableState() {
        super.updateEditableState();
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        ListView listView = viewHolder.entitiesListView;
        NodeListAdapter adapter = ((NodeListAdapter) listView.getAdapter());
        if (adapter != null) {
            adapter.setSelectionEnabled(!isRecordEditLocked());
            adapter.notifyDataSetChanged();
        }
    }

    protected UiInternalNode addNode() {
        return surveyService().addEntity();
    }

    protected UiInternalNode getSelectedNode(int position, UiEntityCollection entityCollection) {
        return (UiInternalNode) entityCollection.getChildAt(position);
    }

    public void onSelected() {
        super.onSelected();
        updateValidationErrorMessage();
    }

    private void updateValidationErrorMessage() {
        UiEntityCollection node = node();
        if (node.getValidationErrors() == null || node.getValidationErrors().isEmpty()) {
            Views.hide(validationMessagesContainer);
        }  else {
            Views.show(validationMessagesContainer);
            String errorMessage = StringUtils.join(node.getValidationErrors(), "\n");
            validationMessagesTextView.setText(errorMessage);
        }
    }
}
