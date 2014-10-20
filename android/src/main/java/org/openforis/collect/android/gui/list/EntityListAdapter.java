package org.openforis.collect.android.gui.list;

import android.view.*;
import android.widget.CheckBox;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.detail.NodeDeleter;
import org.openforis.collect.android.util.StringUtils;
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
    private final NodeDeleter nodeDeleter;

    private Set<UiNode> nodesToEdit = new HashSet<UiNode>();
    private Set<CheckBox> checked = new HashSet<CheckBox>();
    private ActionMode actionMode;

    public EntityListAdapter(SurveyNodeActivity activity, UiInternalNode parentNode, NodeDeleter nodeDeleter) {
        super(activity, parentNode);
        this.nodeDeleter = nodeDeleter;
    }

    public String getText(UiNode node) {
        List<UiAttribute> attributes = getKeyAttributes(node);
        if (attributes.isEmpty())
            attributes = allChildAttributes(node);
        return toNodeLabel(attributes);
    }

    private List<UiAttribute> getKeyAttributes(UiNode child) {
        if (child instanceof UiEntity)
            return ((UiEntity) child).getKeyAttributes();
        if (child instanceof UiRecord.Placeholder)
            return ((UiRecord.Placeholder) child).getKeyAttributes();
        throw new IllegalStateException("Unexpected node type. Expected UiEntity or UiRecord.Placeholder, was " + child.getClass());
    }

    protected int layoutResourceId() {
        return LAYOUT_RESOURCE_ID;
    }

    protected void onPrepareView(final UiNode node, View row) {
        final CheckBox checkbox = (CheckBox) row.findViewById(R.id.nodeSelectedForAction);
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

    private String toNodeLabel(List<UiAttribute> attributes) {
        StringBuilder s = new StringBuilder();
        for (Iterator<UiAttribute> iterator = attributes.iterator(); iterator.hasNext(); ) {
            // TODO: Should assemble the attribute name/value manually,
            // to so "Unspecified" can be picked up from a resource, and different parts can be styled separately
            UiAttribute attribute = iterator.next();
            String value = attribute.valueAsString();
            value = value == null ? activity.getResources().getString(R.string.label_unspecified) : value;
            s.append(StringUtils.ellipsisMiddle(attribute.getLabel(), MAX_ATTRIBUTE_LABEL_LENGTH)).append(": ")
                    .append(StringUtils.ellipsisMiddle(value, MAX_ATTRIBUTE_VALUE_LENGTH));
            if (iterator.hasNext())
                s.append('\n');
        }
        return s.toString();
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
            switch (item.getItemId()) {
                case R.id.delete_selected_nodes:
                    nodeDeleter.delete(nodesToEdit);
                    // Need to clear the back stack, to prevent deleted node from being revisited.
                    ((SurveyNodeActivity) activity).reloadWithoutBackStack();

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
}
