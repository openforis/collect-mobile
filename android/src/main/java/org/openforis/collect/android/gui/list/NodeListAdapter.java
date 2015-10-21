package org.openforis.collect.android.gui.list;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class NodeListAdapter extends BaseAdapter {
    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_node;
    protected final Activity activity;
    protected final UiInternalNode parentNode;
    private final Attrs attrs;
    private List<UiNode> nodes;


    public NodeListAdapter(Activity activity, UiInternalNode parentNode) {
        this.activity = activity;
        this.parentNode = parentNode;
        this.nodes = new ArrayList<UiNode>(parentNode.getChildren());
        this.attrs = new Attrs(this.activity);
    }

    public UiNode getItem(int position) {
        return nodes.get(position);
    }

    public int getCount() {
        return nodes.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NodeHolder holder;
        if (row == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(layoutResourceId(), parent, false);
            if (AndroidVersion.greaterThan10())
                setBackground(row);

            holder = new NodeHolder();
            holder.text = (TextView) row.findViewById(R.id.nodeLabel);
            holder.status = (ImageView) row.findViewById(R.id.nodeStatus);

            row.setTag(holder);
        } else {
            holder = (NodeHolder) row.getTag();
        }

        UiNode node = nodes.get(position);
        holder.text.setText(getText(node));
        if (!node.isRelevant())
            holder.text.setTextColor(attrs.color(R.attr.irrelevantTextColor));
        else
            holder.text.setTextColor(attrs.color(R.attr.relevantTextColor));
        holder.status.setImageResource(iconResource(node));
        onPrepareView(node, row);

        return row;
    }

    protected void onPrepareView(UiNode node, View row) {

    }

    protected int layoutResourceId() {
        return LAYOUT_RESOURCE_ID;
    }

    private void setBackground(View row) {
        row.setBackgroundResource(attrs.resourceId(android.R.attr.activatedBackgroundIndicator));
    }


    public String getText(UiNode node) {
        return node.getLabel();
    }

    public void notifyDataSetChanged() {
        this.nodes = new ArrayList<UiNode>(parentNode.getChildren());
        super.notifyDataSetChanged();
    }

    private int iconResource(UiNode node) {
        if (!node.isRelevant())
            return 0;
        switch (node.getStatus()) {
            case OK:
            case EMPTY:
                return 0;
            case VALIDATION_WARNING:
                return R.drawable.yellow_circle;
            case VALIDATION_ERROR:
                return R.drawable.red_circle;
            default:
                throw new IllegalStateException("Unexpected node status: " + node.getStatus());
        }
    }

    private static class NodeHolder {
        TextView text;
        ImageView status;
    }
}
