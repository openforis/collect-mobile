package org.openforis.collect.android.gui.list;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.openforis.collect.android.gui.util.Views.hide;
import static org.openforis.collect.android.gui.util.Views.px;

/**
 * @author Daniel Wiell
 */
public class NodeListAdapter extends BaseAdapter {
    private static final int LAYOUT_RESOURCE_ID = R.layout.listview_node;
    protected final FragmentActivity activity;
    protected final UiInternalNode parentNode;
    private final Attrs attrs;
    private List<UiNode> nodes;

    public NodeListAdapter(FragmentActivity activity, UiInternalNode parentNode) {
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
        UiNode node = nodes.get(position);
        NodeHolder holder;
        boolean newRow;
        if (row == null) {
            newRow = true;
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(layoutResourceId(), parent, false);
            if (AndroidVersion.greaterThan10())
                setBackground(row);

            holder = new NodeHolder();
            holder.row = row;
            holder.text = row.findViewById(R.id.nodeLabel);
            holder.status = row.findViewById(R.id.nodeStatus);

            row.setTag(holder);
        } else {
            newRow = false;
            holder = (NodeHolder) row.getTag();
        }

        if (holder.text != null) {
            holder.text.setText(getText(node));
            setRowStyle(holder, node, newRow);
        }
        holder.status.setImageResource(iconResource(node));
        onPrepareView(node, row);

        return row;
    }

    protected void setRowStyle(final NodeHolder holder, UiNode node, boolean newRow) {
        Typeface typeface =
                node instanceof UiRecordCollection || node instanceof UiEntityCollection || node instanceof UiEntity
                    ? Typeface.DEFAULT_BOLD
                    : Typeface.DEFAULT;
        holder.text.setTypeface(typeface);

        Definition def = node.getDefinition();
        boolean visible = node.isRelevant() &&
                !(def instanceof UiAttributeDefinition && ((UiAttributeDefinition) def).hidden);
        final View view = holder.row;

        toggleListItemVisibility(holder, view, visible, !newRow, new Runnable() {
            public void run() {
                holder.fadingIn = holder.fadingOut = false;
            }
        });
    }

    private void toggleListItemVisibility(final NodeHolder holder, final View listItem, boolean visible, boolean animate, final Runnable animationEndCallback) {
        final int newVisibility = visible ? View.VISIBLE : View.INVISIBLE;
        final int initialMinHeight = visible ? 1 : px(activity, 48);
        final int finalMinHeight = visible ? px(activity, 48) : 1;
        final int finalLayoutHeight = visible ? ViewGroup.LayoutParams.WRAP_CONTENT : 1;
        final AbsListView.LayoutParams finalLayoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, finalLayoutHeight);

        if (animate) {
            int oldVisibility = listItem.getVisibility();

            if (oldVisibility != newVisibility && !holder.fadingIn && !holder.fadingOut //not animating
                    || holder.fadingIn && !visible
                    || holder.fadingOut && visible
                    ) {
                holder.cancelAnimations();
                holder.fadingIn = visible;
                holder.fadingOut = !visible;

                //change min height with animator
                ValueAnimator minHeightAnimator = ValueAnimator.ofInt(initialMinHeight, finalMinHeight);
                minHeightAnimator.setDuration(1000);
                minHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        listItem.setMinimumHeight((Integer) animator.getAnimatedValue());
                    }
                });
                holder.animators = new AnimatorSet();
                holder.animators.setDuration(1000);
                holder.animators.play(minHeightAnimator);
                holder.animators.start();

                //scale up/down with animation
                final int animResId = visible ? R.anim.scale_up : R.anim.scale_down;
                Animation scaleAnimation = AnimationUtils.loadAnimation(activity, animResId);

                final AbsListView.LayoutParams initialLayoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                        listItem.setVisibility(View.VISIBLE);
                        listItem.setLayoutParams(initialLayoutParams);
                    }

                    public void onAnimationEnd(Animation animation) {
                        listItem.setLayoutParams(finalLayoutParams);
                        listItem.setVisibility(newVisibility);
                        animationEndCallback.run();
                    }

                    public void onAnimationRepeat(Animation animation) {}
                });

                holder.animations = new AnimationSet(true);
                holder.animations.setDuration(1000);
                holder.animations.addAnimation(scaleAnimation);

                listItem.startAnimation(holder.animations);
            }
        } else if (!visible) {
            listItem.setMinimumHeight(finalMinHeight);
            listItem.setLayoutParams(finalLayoutParams);
            hide(listItem);
        }
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

    public List<UiAttribute> getSummaryAttributes(UiNode node) {
        return Collections.emptyList();
    }

    protected String toNodeLabel(UiAttribute attribute) {
        return attribute.valueAsString();
    }

    public void notifyDataSetChanged() {
        this.nodes = new ArrayList<UiNode>(parentNode.getChildren());
        super.notifyDataSetChanged();
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

    private static class NodeHolder {
        View row;
        TextView text;
        ImageView status;
        boolean fadingIn;
        boolean fadingOut;
        AnimatorSet animators;
        AnimationSet animations;

        void cancelAnimations() {
            row.clearAnimation();
            if (animators != null) {
                animators.cancel();
            }
            if (animations != null) {
                animations.cancel();
            }
        }
    }
}
