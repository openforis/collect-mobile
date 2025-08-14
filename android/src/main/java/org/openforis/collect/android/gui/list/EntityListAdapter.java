package org.openforis.collect.android.gui.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;
import org.openforis.collect.android.viewmodel.UiDateAttribute;
import org.openforis.collect.android.viewmodel.UiEntityCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodes;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Daniel Wiell
 */
public class EntityListAdapter extends NodeListAdapter {
    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_entity;
    public static final int MAX_SUMMARY_ATTRIBUTES = 3;
    public static final int MAX_ATTRIBUTE_LABEL_LENGTH = 20;
    public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 20;

    private final Set<UiNode> nodesToEdit = new HashSet<UiNode>();
    private final CodeListService codeListService;
    private final boolean records;
    private ActionMode actionMode;

    public EntityListAdapter(SurveyNodeActivity activity, boolean records, UiInternalNode parentNode) {
        super(activity, parentNode);
        this.records = records;
        codeListService = ServiceLocator.codeListService();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        LinearLayout summaryContainer = (LinearLayout) row.findViewById(R.id.nodeSummaryAttributesContainer);

        UiNode node = getItem(position);
        List<String> summaryAttributeValues = getSummaryAttributeValues(node);

        if (summaryContainer.getChildCount() == 0) {
            //add summary text views
            summaryContainer.setWeightSum(summaryAttributeValues.size());
            for (int i = 0; i < summaryAttributeValues.size(); i++) {
                TextView textView = new TextView(activity);
                //same width for every summary item
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = 1;
                textView.setLayoutParams(p);
                summaryContainer.addView(textView);
            }
        }

        for (int i = 0; i < summaryContainer.getChildCount(); i++) {
            TextView textView = (TextView) summaryContainer.getChildAt(i);
            textView.setText(summaryAttributeValues.get(i));
        }

        //modified on
        TextView modifiedOnTextView = (TextView) row.findViewById(R.id.nodeModifiedOnLabel);
        if (node instanceof UiRecord.Placeholder) {
            modifiedOnTextView.setText(DateUtils.getRelativeTimeSpanString(node.getModifiedOn().getTime()));
        } else {
            modifiedOnTextView.setVisibility(View.GONE);
        }

        CheckBox checkBox = (CheckBox) row.findViewById(R.id.nodeSelectedForAction);
        if (checkBox != null) {
            boolean checked = nodesToEdit.contains(node);
            checkBox.setChecked(checked);
        }
        return row;
    }

    @Override
    public void setSelectionEnabled(boolean selectionEnabled) {
        super.setSelectionEnabled(selectionEnabled);
        if (!selectionEnabled && actionMode != null) {
            actionMode.finish();
        }
    }

    public List<UiAttribute> getSummaryAttributes(UiNode node) {
        return UiNodes.getSummaryAttributes(node);
    }

    private List<String> getSummaryAttributeValues(UiNode node) {
        List<String> result = new ArrayList<String>();
        for (UiAttribute summaryAttr : getSummaryAttributes(node)) {
            String summaryText = toNodeLabel(summaryAttr);
            result.add(summaryText);
        }
        return result;
    }

    protected String toNodeLabel(UiAttribute attribute) {
        String value;
        if (attribute instanceof UiCodeAttribute) {
            value = codeString((UiCodeAttribute) attribute);
        } else if (attribute instanceof UiDateAttribute) {
            value = dateString((UiDateAttribute) attribute);
        } else {
            value = attribute.valueAsString();
        }
        return value == null ? activity.getResources().getString(R.string.label_unspecified) : value;
    }

    private String codeString(UiCodeAttribute attribute) {
        UiCode code = attribute.getCode();
        if (code != null && code.getLabel() == null
                && !(parentNode instanceof UiRecordCollection)) { // Don't look up code labels for record collection
            UiCodeList codeList = codeListService.codeList(attribute);
            attribute.setCode(codeList.getCode(attribute.getCode().getValue()));
        }
        return attribute.valueAsString();
    }

    private String dateString(UiDateAttribute attribute) {
        Date date = attribute.getDate();
        return date == null ? null : DateFormatUtils.format(date, activity.getString(R.string.entity_list_date_pattern));
    }

    protected int layoutResourceId() {
        return LAYOUT_RESOURCE_ID;
    }

    protected void onPrepareView(final UiNode node, View row) {
        final CheckBox checkbox = row.findViewById(R.id.nodeSelectedForAction);
        Definition parentDef = node.getParent().getDefinition();
        if ((!(node instanceof UiRecord.Placeholder) && !isSelectionEnabled()) ||
                (parentDef instanceof UiEntityCollectionDefinition &&
                ((UiEntityCollectionDefinition) parentDef).isEnumerated())
        ) {
            Views.hide(checkbox);
        } else {
            Views.show(checkbox);
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (checkbox.isChecked()) {
                        nodesToEdit.add(node);
                    } else {
                        nodesToEdit.remove(node);
                    }

                    if (!nodesToEdit.isEmpty()) {
                        if (actionMode == null)
                            actionMode = activity.startActionMode(new EditCallback());
                        else
                            setEditTitle(actionMode);
                    }
                    if (nodesToEdit.isEmpty() && actionMode != null)
                        actionMode.finish();
                }
            });
        }
    }

    private void setEditTitle(ActionMode mode) {
        mode.setTitle(activity.getString(R.string.amount_selected, nodesToEdit.size()));
    }

    private class EditCallback implements ActionMode.Callback {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.entity_action_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            setEditTitle(mode);
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ArrayList<Integer> nodeIdsToRemove = new ArrayList<Integer>();
            for (UiNode uiNode : nodesToEdit)
                nodeIdsToRemove.add(uiNode.getId());
            if (item.getItemId() == R.id.delete_selected_nodes) {
                DeleteConfirmationFragment.show(nodeIdsToRemove, records, activity);
                return true;
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            nodesToEdit.clear();
        }
    }

    public static final class DeleteConfirmationFragment extends AppCompatDialogFragment {
        private static final String NODE_IDS_TO_REMOVE = "node_ids_to_remove";
        private static final String REMOVE_RECORDS = "remove_records";

        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ArrayList<Integer> nodeIdsToRemove = getArguments().getIntegerArrayList(NODE_IDS_TO_REMOVE);
            final boolean records = getArguments().getBoolean(REMOVE_RECORDS);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            int titleResId = records ? R.string.delete_records_title : R.string.delete_entities_title;
            String message = getString(R.string.delete_node_confirm_message,
                    nodeIdsToRemove.size(),
                    getString(records ? R.string.part_records: R.string.part_entities));
            builder.setTitle(titleResId)
                    .setMessage(message)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (records)
                                ServiceLocator.surveyService().deleteRecords(nodeIdsToRemove);
                            else
                                ServiceLocator.surveyService().deleteEntities(nodeIdsToRemove);
                            SurveyNodeActivity.restartActivity(getActivity());
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            return builder.create();
        }

        public static void show(ArrayList<Integer> nodeIdsToRemove, boolean records, FragmentActivity activity) {
            DeleteConfirmationFragment fragment = new DeleteConfirmationFragment();
            Bundle arguments = new Bundle();
            fragment.setArguments(arguments);
            arguments.putIntegerArrayList(NODE_IDS_TO_REMOVE, nodeIdsToRemove);
            arguments.putBoolean(REMOVE_RECORDS, records);
            fragment.show(activity.getSupportFragmentManager(), "confirmEntityDeletion");
        }
    }
}
