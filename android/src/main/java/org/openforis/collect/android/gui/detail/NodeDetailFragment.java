package org.openforis.collect.android.gui.detail;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SmartNext;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.NodeListDialogFragment;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.List;
import java.util.Map;


/**
 * @author Daniel Wiell
 */
public abstract class NodeDetailFragment<T extends UiNode> extends Fragment {
    private static final String ARG_RECORD_ID = "record_id";
    private static final String ARG_NODE_ID = "node_id";
    private static final int TEXT_MAX_LINES_TWO_PANE = 8;
    private static final int TEXT_MAX_LINES_SINGLE_PANE = 4;

    private boolean selected;
    private T node;
    private NodeDetailFragmentHolder holder; // do not store it in view tag: it could override the tag set in parent view

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ServiceLocator.init(this.getActivity())) {
            setHasOptionsMenu(true);
            if (!getArguments().containsKey(ARG_NODE_ID))
                throw new IllegalStateException("Missing argument: " + ARG_NODE_ID);
            int recordId = getArguments().getInt(ARG_RECORD_ID);
            int nodeId = getArguments().getInt(ARG_NODE_ID);
            node = lookupNode(recordId, nodeId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createView(inflater, container, savedInstanceState);

        NodeDetailFragmentHolder holder = new NodeDetailFragmentHolder();
        holder.label = view.findViewById(R.id.node_label);
        holder.description = view.findViewById(R.id.node_description);
        holder.prompt = view.findViewById(R.id.node_prompt);
        holder.loader = view.findViewById(R.id.node_loader);

        Definition nodeDef = node.getDefinition();

        setOrRemoveText(holder.label, extractNodeDefLabel() + " ");
        setOrRemoveText(holder.description, nodeDef.description);
        setOrRemoveText(holder.prompt, nodeDef.prompt);

        this.holder = holder;

        return view;
    }

    public void onPause() {
        super.onPause();
    }

    private void setOrRemoveText(TextView textView, String text) {
        if (text == null)
            ((ViewManager) textView.getParent()).removeView(textView);
        else {
            textView.setText(text);
            textView.setMaxLines(isTwoPane() ? TEXT_MAX_LINES_TWO_PANE : TEXT_MAX_LINES_SINGLE_PANE);
            // add vertical scroll
            textView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selected)
            onSelected();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.node_detail_fragment_actions, menu);
        if (isTwoPane())
            menu.removeItem(R.id.action_attribute_list);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        setupNextNodeMenuItem(menu);
        setupSmartNextMenuItem(menu);
        setupPrevNodeMenuItem(menu);

        /*
        SurveyNodeActivity activity = (SurveyNodeActivity) getActivity();
        if (! activity.isTwoPane()) {
            MenuItem attributeListItem = menu.findItem(R.id.action_attribute_list);
            if (attributeListItem != null) {// TODO: This should always be the case?
                MenuItemCompat.setShowAsAction(attributeListItem, SupportMenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
        */
    }

    private void setupPrevNodeMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_prev_attribute);
        if (menuItem != null) {
            List<UiNode> relevantSiblings = node.getRelevantSiblings();
            boolean isFirst = relevantSiblings.indexOf(node) == 0;
            disable(menuItem, isFirst);
        }
    }

    private void setupNextNodeMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next_attribute);
        if (menuItem != null) {
            List<UiNode> relevantSiblings = node.getRelevantSiblings();
            boolean isLast = relevantSiblings.indexOf(node) == relevantSiblings.size() - 1;
            disable(menuItem, isLast);
        }
    }

    private void setupSmartNextMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_smart_next_attribute);
        boolean hasNext = new SmartNext(node).hasNext();
        disable(menuItem, !hasNext);
    }

    private void disable(MenuItem menuItem, boolean disabled) {
        menuItem.setEnabled(!disabled);
        if (disabled) {
            menuItem.getIcon().setAlpha(130);
        } else {
            menuItem.getIcon().setAlpha(255);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_attribute_list) {
            showAttributeListPopup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSuffixToLabel(String suffix) {
        if (this.holder == null) return;
        holder.label.setText(extractNodeDefLabel() + " (" + suffix + ")");
    }

    protected void showAttributeSavingLoader() {
        if (holder == null || holder.loader == null) return;
        holder.loader.setVisibility(View.VISIBLE);
    }

    protected void hideAttributeSavingLoader() {
        if (holder == null || holder.loader == null) return;
        holder.loader.setVisibility(View.INVISIBLE);
    }

    protected void showAttributeSavingError() {
        if (holder == null || holder.loader == null) return;
        holder.loader.setVisibility(View.VISIBLE);
    }

    public void onNodeChanging(UiNode _node) {
        showAttributeSavingLoader();
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    public void onDeselect() {
        selected = false;
        // Empty default implementation
    }

    public void onSelect() {
        selected = true;
        // Empty default implementation
    }

    public void onSelected() {
        View view = getDefaultFocusedView();
        if (view == null || !node.isRelevant())
            hideKeyboard();
        else {
            if (view instanceof EditText && view.isEnabled())
                showKeyboard(view);
            else {
                hideKeyboard();
                view.requestFocus();
            }
        }
    }

    protected View getDefaultFocusedView() {
        return null;
    }

    protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);


    public T node() {
        return node;
    }


    private void showAttributeListPopup() {
        NodeListDialogFragment dialogFragment = new NodeListDialogFragment();
        dialogFragment.setRetainInstance(true);
        dialogFragment.show(getFragmentManager(), null);
    }

    @SuppressWarnings("unchecked")
    private T lookupNode(int recordId, int nodeId) {
        SurveyService surveyService = ServiceLocator.surveyService();
        if (recordId > 0 && !surveyService.isRecordSelected(recordId))
            surveyService.selectRecord(recordId);
        UiNode uiNode = surveyService.lookupNode(nodeId);
        if (uiNode == null) {
            throw new IllegalStateException(String.format("Could not find node with id %d in record %d",
                    nodeId, recordId));
        }
        return (T) uiNode;
    }

    private void showKeyboard(View view) {
        Keyboard.show(view, getActivity());
    }

    private void hideKeyboard() {
        Keyboard.hide(getActivity());
    }

    private boolean isTwoPane() {
        return getActivity() instanceof SurveyNodeActivity && ((SurveyNodeActivity) getActivity()).isTwoPane();
    }

    public static NodeDetailFragment<?> create(UiNode node) {
        Bundle arguments = new Bundle();
        if (!(node instanceof UiRecordCollection)) // TODO: Ugly
            arguments.putInt(ARG_RECORD_ID, node.getUiRecord().getId());
        arguments.putInt(ARG_NODE_ID, node.getId());
        NodeDetailFragment<?> fragment = createInstance(node);
        fragment.setArguments(arguments);
        return fragment;
    }

    private static NodeDetailFragment<?> createInstance(UiNode node) {
        if (node instanceof UiAttribute && node.isCalculated())
            return new CalculatedAttributeFragment();
        if (node instanceof UiAttribute || node instanceof UiAttributeCollection)
            return new SavableNodeDetailFragment();
        if (node instanceof UiEntityCollection)
            return new EntityCollectionDetailFragment();
        if (node instanceof UiRecordCollection)
            return new RecordCollectionDetailFragment();
        if (node instanceof UiInternalNode)
            return new InternalNodeDetailFragment();
        throw new IllegalStateException("Unexpected node type: " + node.getClass());
    }

    private String extractNodeDefLabel() {
        Definition nodeDef = node.getDefinition();
        String label = isTwoPane()
                ? nodeDef.getInterviewLabelOrLabel()
                : nodeDef.label;
        return label;
    }

    private static class NodeDetailFragmentHolder {
        TextView label;
        TextView description;
        TextView prompt;
        ProgressBar loader;
    }
}
