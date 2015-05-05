package org.openforis.collect.android.gui.detail;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.view.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.NodeListDialogFragment;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.*;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/**
 * @author Daniel Wiell
 */
public abstract class NodeDetailFragment<T extends UiNode> extends Fragment {
    private static final String ARG_RECORD_ID = "record_id";
    private static final String ARG_NODE_ID = "node_id";
    private static final int IRRELEVANT_OVERLAY_COLOR = Color.parseColor("#88333333");
    private boolean selected;

    private T node;
    private ViewGroup overlay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (!getArguments().containsKey(ARG_NODE_ID))
            throw new IllegalStateException("Missing argument: " + ARG_NODE_ID);
        int recordId = getArguments().getInt(ARG_RECORD_ID);
        int nodeId = getArguments().getInt(ARG_NODE_ID);
        node = lookupNode(recordId, nodeId);
    }

    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = createView(inflater, container, savedInstanceState);

        setOrRemoveText(rootView, R.id.node_label, node.getLabel() + " ");
        setOrRemoveText(rootView, R.id.node_description, node.getDefinition().description);
        setOrRemoveText(rootView, R.id.node_prompt, node.getDefinition().prompt);

        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.addView(rootView);
        frameLayout.addView(createOverlay());
        return frameLayout;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    private View createOverlay() {
        overlay = new LinearLayout(getActivity());
        overlay.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        overlay.setBackgroundColor(IRRELEVANT_OVERLAY_COLOR);
        updateOverlay();
        return overlay;
    }

    private void setOrRemoveText(View rootView, int textViewId, String text) {
        TextView textView = (TextView) rootView.findViewById(textViewId);
        if (text == null)
            ((ViewManager) textView.getParent()).removeView(textView);
        else
            textView.setText(text);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (selected)
            onSelected();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.node_detail_fragment_actions, menu);
        SurveyNodeActivity activity = (SurveyNodeActivity) getActivity();
        if (activity.isTwoPane())
            menu.removeItem(R.id.action_attribute_list);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem nextAttributeItem = menu.findItem(R.id.action_next_attribute);
        // TODO: Should check this based on complete graph, not just for the entity. This logic should be done elsewhere
        boolean isLast = node.getIndexInParent() == node.getSiblingCount() - 1;
        if (nextAttributeItem != null && isLast) {
            nextAttributeItem.setEnabled(false);
            nextAttributeItem.getIcon().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }

        boolean attributeListDisplayed = getActivity().findViewById(R.id.attribute_list) != null; // TODO: Ugly
        if (!attributeListDisplayed) {
            MenuItem attributeListItem = menu.findItem(R.id.action_attribute_list);
            // TODO: Only show node list if single pane layout - redundant otherwise
            if (attributeListItem != null) {// TODO: This should always be the case?
                MenuItemCompat.setShowAsAction(attributeListItem, SupportMenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
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

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        updateOverlay();
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
            if (view instanceof EditText)
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


    protected T node() {
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
        return (T) surveyService.lookupNode(nodeId);
    }

    private void updateOverlay() {
        if (node != null && overlay != null)
            overlay.setVisibility(node.isRelevant() ? View.INVISIBLE : View.VISIBLE);
    }

    private void showKeyboard(View view) {
        Keyboard.show(view, getActivity());
    }

    private void hideKeyboard() {
        Keyboard.hide(getActivity());
    }

    public static NodeDetailFragment create(UiNode node) {
        Bundle arguments = new Bundle();
        if (!(node instanceof UiRecordCollection)) // TODO: Ugly
            arguments.putInt(ARG_RECORD_ID, node.getUiRecord().getId());
        arguments.putInt(ARG_NODE_ID, node.getId());
        NodeDetailFragment fragment = createInstance(node);
        fragment.setArguments(arguments);
        return fragment;
    }

    private static NodeDetailFragment createInstance(UiNode node) {
        if (node instanceof UiAttribute && ((UiAttribute) node).isCalculated())
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
}
