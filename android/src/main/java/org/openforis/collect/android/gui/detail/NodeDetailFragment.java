package org.openforis.collect.android.gui.detail;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SmartNext;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.NodeListDialogFragment;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.*;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * @author Daniel Wiell
 */
public abstract class NodeDetailFragment<T extends UiNode> extends Fragment {
    private static final String ARG_RECORD_ID = "record_id";
    private static final String ARG_NODE_ID = "node_id";
    //    private static final int IRRELEVANT_OVERLAY_COLOR = Color.parseColor("#88333333");
    private static final int IRRELEVANT_OVERLAY_COLOR = Color.parseColor("#333333");

    protected enum ViewState {
        DEFAULT, LOADING, NOT_RELEVANT;
    }

    private boolean selected;
    private T node;
    private ViewState viewState = ViewState.DEFAULT;
    private View contentFrame;
    private View notRelevantOverlay;
    private View loadingOverlay;

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
        frameLayout.addView(contentFrame = rootView);
        frameLayout.addView(notRelevantOverlay = createNotRelevantOverlay());
        frameLayout.addView(loadingOverlay = createLoadingOverlay());
        return frameLayout;
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        onViewStateChange();
    }

    private View createNotRelevantOverlay() {
        LinearLayout overlay = new LinearLayout(getActivity());
        overlay.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        overlay.setBackgroundColor(IRRELEVANT_OVERLAY_COLOR);
        AppCompatTextView text = new AppCompatTextView(getContext());
        text.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(new Attrs(getContext()).color(R.attr.irrelevantTextColor));
        overlay.setGravity(Gravity.CENTER);
        // TODO: Use String resource
        text.setText(node.getLabel() + "\r\n\r\nNot relevant");
        overlay.addView(text);
        return overlay;
    }

    private View createLoadingOverlay() {
        LinearLayout overlay = new LinearLayout(getActivity());
        overlay.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        overlay.setGravity(Gravity.CENTER);
        ProgressBar pb = new ProgressBar(getActivity());
        pb.setIndeterminate(true);
        pb.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        overlay.addView(pb);
        Views.hide(overlay);
        return overlay;
    }

    protected void setViewState(ViewState viewState) {
        this.viewState = viewState;
        onViewStateChange();
    }

    protected void onViewStateChange() {
        switch(viewState) {
            case LOADING:
                showFrame(loadingOverlay);
                break;
            case NOT_RELEVANT:
                showFrame(notRelevantOverlay);
                break;
            default:
                showFrame(contentFrame);
        }
    }

    protected void showFrame(View view) {
        FrameLayout mainLayout = (FrameLayout) getView();
        if (mainLayout != null) {
            for (int i = 0; i < mainLayout.getChildCount(); i++) {
                View child = mainLayout.getChildAt(i);
                if (child != view) {
                    Views.hide(child);
                }
            }
            Views.show(view);
        }
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
        toggleNotRelevantOverlayVisibility();
        onViewStateChange();
        if (selected)
            onSelected();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.node_detail_fragment_actions, menu);
        SurveyNodeActivity activity = (SurveyNodeActivity) getActivity();
        if (activity.isTwoPane())
            menu.removeItem(R.id.action_attribute_list);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        setupNextNodeMenuItem(menu);
        setupSmartNextMenuItem(menu);
        setupPrevNodeMenuItem(menu);

        boolean attributeListDisplayed = getActivity().findViewById(R.id.attribute_list) != null; // TODO: Ugly
        if (!attributeListDisplayed) {
            MenuItem attributeListItem = menu.findItem(R.id.action_attribute_list);
            if (attributeListItem != null) {// TODO: This should always be the case?
                MenuItemCompat.setShowAsAction(attributeListItem, SupportMenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    private void setupPrevNodeMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_prev_attribute);
        boolean isFirst = node.getIndexInParent() == 0;
        if (menuItem != null && isFirst)
            disable(menuItem);
    }

    private void setupNextNodeMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next_attribute);
        boolean isLast = node.getIndexInParent() == node.getSiblingCount() - 1;
        if (menuItem != null && isLast)
            disable(menuItem);
    }

    private void setupSmartNextMenuItem(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_smart_next_attribute);
        if (!new SmartNext(node).hasNext())
            disable(menuItem);
    }

    private void disable(MenuItem menuItem) {
        menuItem.setEnabled(false);
        menuItem.getIcon().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
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
        toggleNotRelevantOverlayVisibility();
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

    private void toggleNotRelevantOverlayVisibility() {
        if (node != null && notRelevantOverlay != null) {
            boolean relevant = node.isRelevant();
            setViewState(relevant ? ViewState.DEFAULT: ViewState.NOT_RELEVANT);
        }
    }

    protected void processSlowTask(final Runnable runnable) {
        setViewState(ViewState.LOADING);

        new SlowProcessTask(this, runnable).execute();
    }

    private static class SlowProcessTask extends AsyncTask<Void, Void, Void> {

        private final NodeDetailFragment fragment;
        private final Runnable runnable;

        SlowProcessTask(NodeDetailFragment fragment, Runnable runnable) {
            super();
            this.fragment = fragment;
            this.runnable = runnable;
        }

        protected Void doInBackground(Void... voids) {
            runnable.run();
            //restore content frame, wait for the navigation to complete
            new Timer().schedule(new TimerTask() {
                public void run() {
                    fragment.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            fragment.setViewState(ViewState.DEFAULT);
                        }
                    });
                }
            }, 1000);
            return null;
        }
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
