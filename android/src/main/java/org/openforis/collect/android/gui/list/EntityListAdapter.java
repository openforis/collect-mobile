package org.openforis.collect.android.gui.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.format.DateUtils;
import android.view.*;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.*;

import java.util.*;


/**
 * @author Daniel Wiell
 */
public class EntityListAdapter extends NodeListAdapter {
    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_entity;
    private static final int MAX_ATTRIBUTES = 2;
    public static final int MAX_ATTRIBUTE_LABEL_LENGTH = 20;
    public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 20;

    private final Set<UiNode> nodesToEdit = new HashSet<UiNode>();
    private final Set<CheckBox> checked = new HashSet<CheckBox>();
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

        //summary/key attributes
        List<String> summaryAttributeValues = getSummaryAttributeValues(node);
        summaryContainer.setWeightSum(summaryAttributeValues.size());

        if (summaryContainer.getChildCount() == 0) {
            for (String summaryAttrVal : summaryAttributeValues) {
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
            modifiedOnTextView.setVisibility(View.INVISIBLE);
        }
        return row;
    }

    public List<UiAttribute> getSummaryAttributes(UiNode node) {
        List<UiAttribute> keyAttributes = getKeyAttributes(node);
        if (keyAttributes.isEmpty())
            keyAttributes = allChildAttributes(node);
        return keyAttributes;
    }

    private List<String> getSummaryAttributeValues(UiNode node) {
        List<String> result = new ArrayList<String>();
        for (UiAttribute summaryAttr : getSummaryAttributes(node)) {
            String summaryText = toNodeLabel(summaryAttr);
            result.add(summaryText);
        }
        return result;
    }

    private List<UiAttribute> getKeyAttributes(UiNode child) {
        if (child instanceof UiEntity)
            return ((UiEntity) child).getKeyAttributes();
        if (child instanceof UiRecord.Placeholder)
            return ((UiRecord.Placeholder) child).getKeyAttributes();
        throw new IllegalStateException("Unexpected node type. Expected UiEntity or UiRecord.Placeholder, was " + child.getClass());
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

    private List<UiAttribute> allChildAttributes(UiNode node) {
        List<UiAttribute> attributes = new ArrayList<UiAttribute>();
        if (node instanceof UiInternalNode) {
            for (UiNode potentialAttribute : ((UiInternalNode) node).getChildren()) {
                if (potentialAttribute instanceof UiAttribute)
                    attributes.add((UiAttribute) potentialAttribute);
                if (attributes.size() >= MAX_ATTRIBUTES)
                    return attributes;
            }
        }
        return attributes;
    }

    protected void setTypeface(TextView text, UiNode node) {
    }

    protected void onPrepareView(final UiNode node, View row) {
        final CheckBox checkbox = (CheckBox) row.findViewById(R.id.nodeSelectedForAction);
        Definition parentDef = node.getParent().getDefinition();
        if (parentDef instanceof UiEntityCollectionDefinition &&
                ((UiEntityCollectionDefinition) parentDef).isEnumerated()) {
            Views.hide(checkbox);
        } else {
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (checkbox.isChecked()) {
                        nodesToEdit.add(node);
                        checked.add(checkbox);
                    } else {
                        nodesToEdit.remove(node);
                        checked.remove(checkbox);
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
            switch (item.getItemId()) {
                case R.id.delete_selected_nodes:
                    DeleteConfirmationFragment.show(nodeIdsToRemove, records, activity);
//                    nodeDeleter.delete(nodesToEdit);
//                    // Need to clear the back stack, to prevent deleted node from being revisited.
//                    ((SurveyNodeActivity) activity).reloadWithoutBackStack();

                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            nodesToEdit.clear();
            for (CheckBox checkBox : checked) {
                checkBox.setChecked(false);
                checkBox.setSelected(false);
            }
        }
    }

    public static final class DeleteConfirmationFragment extends AppCompatDialogFragment {
        private static final String NODE_IDS_TO_REMOVE = "node_ids_to_remove";
        private static final String REMOVE_RECORDS = "remove_records";

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ArrayList<Integer> nodeIdsToRemove = getArguments().getIntegerArrayList(NODE_IDS_TO_REMOVE);
            final boolean records = getArguments().getBoolean(REMOVE_RECORDS);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // TODO: Use string resource
            String title = records ? "Delete records" : "Delete entities";
            String message = "You are about to delete " + nodeIdsToRemove.size() + (records ? " records" : " entities.");
            builder.setTitle(title)
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
