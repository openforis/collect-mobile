package org.openforis.collect.android.gui.list;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public class SimpleNodeListAdapter extends RecyclerView.Adapter<SimpleNodeListAdapter.NodeHolder> {
    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_node;
    protected final FragmentActivity activity;
    protected final UiInternalNode parentNode;
    private List<UiNode> nodes;
    private UiNode selectedNode;
    private OnItemClickListener onItemClickListener;

    public SimpleNodeListAdapter(FragmentActivity activity, UiInternalNode parentNode, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.parentNode = parentNode;
        this.nodes = parentNode.getRelevantChildren();
        this.onItemClickListener = onItemClickListener;
    }

    public UiNode getItem(int position) {
        return nodes.get(position);
    }

    public int getItemCount() {
        return nodes.size();
    }

    public NodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(LAYOUT_RESOURCE_ID, parent, false);
        return new NodeHolder(v);
    }

    public void onBindViewHolder(final NodeHolder holder, int position) {
        final UiNode node = nodes.get(position);

        if (holder.text != null) {
            holder.text.setText(getText(node));
            if (isNodeCollection(node))
                holder.text.setTypeface(Typeface.DEFAULT_BOLD);
            else
                holder.text.setTypeface(Typeface.DEFAULT);
        }
        holder.status.setImageResource(iconResource(node));

        boolean selected = selectedNode == node;
        holder.row.setSelected(selected);

        holder.row.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int position = holder.getAdapterPosition();
                    onItemClickListener.onItemClick(position, node);
                }
            }
        });
    }

    public String getText(UiNode node) {
        String prefix = isNodeCollection(node) ? "+ ": "";
        return prefix + node.getLabel();
    }

    public List<UiNode> getNodes() {
        return nodes;
    }

    public void insert(int position, UiNode node) {
        nodes.add(position, node);
        notifyItemInserted(position);
    }

    public void remove(UiNode node) {
        int position = nodes.indexOf(node);
        if (position >= 0) {
            nodes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void selectNode(UiNode node) {
        unselectNode();
        selectedNode = node;
        notifyNodeChanged(selectedNode);
    }

    public void unselectNode() {
        UiNode oldSelectedNode = selectedNode;
        selectedNode = null;
        if (oldSelectedNode != null) {
            notifyNodeChanged(oldSelectedNode);
        }
    }

    public void notifyNodeChanged(UiNode node) {
        int index = nodes.indexOf(node);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    private boolean isNodeCollection(UiNode node) {
        return node instanceof UiRecordCollection || node instanceof UiEntityCollection || node instanceof UiEntity;
    }

    private int iconResource(UiNode node) {
        if (!node.isRelevant())
            return 0;
        switch (node.getStatus()) {
            case VALIDATION_WARNING:
                return R.drawable.ic_warning_yellow_24dp;
            case VALIDATION_ERROR:
                return R.drawable.ic_error_red_24dp;
            default:
                return 0;
        }
    }

    public static class NodeHolder extends RecyclerView.ViewHolder {
        LinearLayout row;
        TextView text;
        ImageView status;

        NodeHolder(LinearLayout view) {
            super(view);
            this.row = view;
            this.text = (TextView) this.row.findViewById(R.id.nodeLabel);
            this.status = (ImageView) this.row.findViewById(R.id.nodeStatus);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(int position, UiNode node);

    }

}
