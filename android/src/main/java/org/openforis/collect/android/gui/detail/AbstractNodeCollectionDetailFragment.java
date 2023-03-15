package org.openforis.collect.android.gui.detail;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.EntityListAdapter;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Tasks;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiEntityCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Daniel Wiell
 */
public abstract class AbstractNodeCollectionDetailFragment<T extends UiInternalNode> extends NodeDetailFragment<T> {

    private static final Typeface HEADER_TYPEFACE = Typeface.DEFAULT_BOLD;
    private EntityListAdapter adapter;
    private Timer adapterUpdateTimer;

    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_entity_collection_detail, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view.getTag() == null) {
            ViewHolder holder = new ViewHolder();
            holder.addButtonSwitcher = (ViewSwitcher) view.findViewById(R.id.add_button_switcher);
            if (holder.addButtonSwitcher != null) {
                if (isEnumeratedEntityCollection()) {
                    Views.hide(holder.addButtonSwitcher);
                } else {
                    initializeAddButton(holder);
                }
            }
            view.setTag(holder);
        }
    }

    public void initializeAddButton(final ViewHolder h) {
        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

        // set the animation type to ViewSwitcher
        h.addButtonSwitcher.setInAnimation(in);
        h.addButtonSwitcher.setOutAnimation(out);

        h.addButton = h.addButtonSwitcher.findViewById(R.id.action_add_node);
        h.addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setEnabled(false);
                if (isMaxItemsLimitReached()) {
                    String message = getActivity().getString(R.string.entity_collection_cannot_add_more_items, getMaxLimit());
                    Dialogs.alert(getActivity(), getString(R.string.warning), message);
                    v.setEnabled(true);
                } else {
                    startAddNodeTask(h);
                }
            }
        });
    }

    @Override
    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        super.onNodeChange(node, nodeChanges);
        boolean changedChildNode = node.getParent().equals(node());  // TODO: If removed, can we rely on parent to be present?
        if (changedChildNode || nodeChanges.containsKey(node()))
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapterUpdateTimer != null)
            adapterUpdateTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupNodeCollection(getView());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.entity_collection_details_fragment_actions, menu);
        SurveyNodeActivity activity = (SurveyNodeActivity) getActivity();
        if (activity.isTwoPane())
            menu.removeItem(R.id.action_attribute_list);

    }

    protected abstract UiInternalNode addNode();

    protected abstract UiInternalNode getSelectedNode(int position, T nodeCollection);

    protected SurveyService surveyService() {
        return ServiceLocator.surveyService();
    }

    private void startAddNodeTask(final ViewHolder h) {
        switchToProcessingAddButton(h);

        Runnable task = new Runnable() {
            public void run() {
                final UiInternalNode newNode = addNode();
                nodeNavigator().navigateTo(newNode.getFirstChild().getId());
                switchToIdleAddButton(h);
            }
        };
        if (node() instanceof UiRecordCollection) {
            Tasks.runSlowTask(getActivity(), task);
        } else {
            task.run();
        }
    }

    private void switchToProcessingAddButton(final ViewHolder h) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                h.addButton.setEnabled(false);
                h.addButtonSwitcher.showNext();
            }
        });
    }

    private void switchToIdleAddButton(final ViewHolder h) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                h.addButton.setEnabled(true);
                h.addButtonSwitcher.showPrevious();
            }
        });
    }

    private void startEditNodeTask(final int position) {
        final T nodeCollection = node();
        Runnable task = new Runnable() {
            public void run() {
                UiInternalNode selectedNode = getSelectedNode(position, nodeCollection);
                UiNode firstEditableChild = selectedNode.getFirstEditableChild();
                if (firstEditableChild != null) {
                    nodeNavigator().navigateTo(firstEditableChild.getId());
                }
            }
        };
        if (nodeCollection instanceof UiRecordCollection) {
            Tasks.runSlowTask(getActivity(), task);
        } else {
            task.run();
        }
    }

    private void setupNodeCollection(View rootView) {
        if (adapter == null) {
            adapter = new EntityListAdapter((SurveyNodeActivity) getActivity(), this instanceof RecordCollectionDetailFragment, node());
        } else {
            adapter.notifyDataSetChanged();
        }
        ListView listView = (ListView) rootView.findViewById(R.id.entity_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startEditNodeTask(position);
            }
        });

        //manage dynamic header visibility
        boolean headerVisible = ! node().getChildren().isEmpty();
        Views.toggleVisibility(rootView, R.id.entity_list_header_wrapper, headerVisible);
        if (headerVisible) {
            buildDynamicHeaderPart(rootView);
            if (isEnumeratedEntityCollection()) {
                Views.hide(rootView, R.id.entity_list_header_selection_checkbox);
            }
        }

        adapterUpdateTimer = new Timer();
        adapterUpdateTimer.schedule(new AdapterUpdaterTask(), 60000, 60000);
    }

    private void buildDynamicHeaderPart(View rootView) {
        LinearLayout header = (LinearLayout) rootView.findViewById(R.id.entity_list_header);
        header.removeAllViews();
        List<String> headings = getDynamicHeadings();
        if (! headings.isEmpty()) {
            header.setWeightSum(headings.size());
            for (String heading : headings) {
                TextView textView = new TextView(getContext());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = 1;
                textView.setLayoutParams(p);
                textView.setMaxLines(2);
                textView.setText(heading);
                textView.setTypeface(HEADER_TYPEFACE);
                header.addView(textView);
            }
            UiNode firstChild = node().getChildAt(0);
            boolean includeModifiedDate = firstChild instanceof UiRecord.Placeholder;
            TextView modifiedDateTextView = (TextView) rootView.findViewById(R.id.entity_list_header_modified_date);
            modifiedDateTextView.setTypeface(HEADER_TYPEFACE);
            Views.toggleVisibility(modifiedDateTextView, includeModifiedDate);
        }
    }

    private List<String> getDynamicHeadings() {
        List<String> headings = new ArrayList<String>();
        T node = node();
        List<UiNode> children = node.getChildren();
        if (! children.isEmpty()) {
            UiNode firstChild = children.get(0);
            List<UiAttribute> summaryAttributes = getSummaryAttributes(firstChild);
            for (UiAttribute attr : summaryAttributes) {
                String heading = attr.getDefinition().label;
                headings.add(heading);
            }
        }
        return headings;
    }

    private List<UiAttribute> getSummaryAttributes(UiNode node) {
        List<UiAttribute> keyAttributes = getKeyAttributes(node);
        if (!keyAttributes.isEmpty()) {
            return keyAttributes;
        } else if (node instanceof UiEntity) {
            List<UiAttribute> summaryAttributes = new ArrayList<UiAttribute>();
            for (UiNode child : ((UiEntity) node).getChildren()) {
                if (child instanceof  UiAttribute &&
                        summaryAttributes.size() < EntityListAdapter.MAX_SUMMARY_ATTRIBUTES) {
                    summaryAttributes.add((UiAttribute) child);
                }
            }
            return summaryAttributes;
        } else {
            return Collections.emptyList();
        }
    }

    private List<UiAttribute> getKeyAttributes(UiNode node) {
        List<UiAttribute> keyAttributes;
        if (node instanceof UiRecord.Placeholder) {
            keyAttributes = ((UiRecord.Placeholder) node).getKeyAttributes();
        } else {
            keyAttributes = ((UiEntity) node).getKeyAttributes();
        }
        return keyAttributes;
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }

    private boolean isEnumeratedEntityCollection() {
        UiNode selectedNode = surveyService().selectedNode();
        Definition nodeDef = selectedNode.getDefinition();
        return nodeDef instanceof UiEntityCollectionDefinition && ((UiEntityCollectionDefinition) nodeDef).isEnumerated();
    }

    private boolean isMaxItemsLimitReached() {
        UiNode selectedNode = surveyService().selectedNode();
        Integer maxLimit = getMaxLimit();
        return maxLimit != null && selectedNode instanceof UiEntityCollection &&
                ((UiEntityCollection) selectedNode).getChildCount() >= maxLimit;
    }

    private Integer getMaxLimit() {
        UiNode selectedNode = surveyService().selectedNode();
        if (selectedNode.getDefinition() instanceof UiEntityCollectionDefinition) {
            UiEntityCollectionDefinition def = (UiEntityCollectionDefinition) selectedNode.getDefinition();
            return def.getFixedMaxCount();
        } else {
            return null;
        }
    }

    private class AdapterUpdaterTask extends TimerTask {

        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class ViewHolder {
        ViewSwitcher addButtonSwitcher;
        View addButton;

    }
}
