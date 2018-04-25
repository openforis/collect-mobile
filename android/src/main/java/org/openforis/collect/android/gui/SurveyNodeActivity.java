package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.openforis.collect.R;
import org.openforis.collect.android.NodeEvent;
import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.detail.ExportDialogFragment;
import org.openforis.collect.android.gui.entitytable.EntityTableDialogFragment;
import org.openforis.collect.android.gui.input.FileAttributeComponent;
import org.openforis.collect.android.gui.list.SimpleNodeListFragment;
import org.openforis.collect.android.gui.pager.NodePagerFragment;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;
import org.openforis.collect.android.viewmodel.UiSurvey;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class SurveyNodeActivity extends BaseActivity implements SurveyListener, NodeNavigator {
    public static final int IMAGE_CAPTURE_REQUEST_CODE = 6385;
    public static final int IMAGE_SELECTED_REQUEST_CODE = 6386;

    private static final String ARG_NODE_ID = "node_id";
    private static final String ARG_RECORD_ID = "record_id";
    private static final int TWO_PANE_MIN_SCREEN_WIDTH = 600;

    private LayoutDependentSupport support;
    private SurveyService surveyService;

    private UiNode selectedNode;
    private FileAttributeComponent imageListener;

    private boolean twoPane;

    public static void startClearSurveyNodeActivity(Context context) {
        Activities.startNewClearTask(context, SurveyNodeActivity.class);
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (ServiceLocator.init(this)) {
            surveyService = ServiceLocator.surveyService();
            selectedNode = selectInitialNode(savedState); // TODO: Ugly that we have to wait with registering the listener, not to get this callback
            support = createLayoutSupport();
            setTitle(selectedNode.getUiSurvey().getLabel());
            enableUpNavigationIfNeeded(selectedNode);
            surveyService.setListener(this);
            support.onCreate(savedState);

            BackStackLimiter.enqueue(this);
        } else {
            navigateToMainPage();
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.node_activity_actions, menu);
        return true;
    }

    public boolean isTwoPane() {
        return twoPane;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNodeSelected(final UiNode previous, final UiNode selected) {
        nodePagerFragment().getView().post(new Runnable() {
            public void run() {
                NodePagerFragment nodePagerFragment = nodePagerFragment();
                if (nodePagerFragment != null)
                    nodePagerFragment.onNodeSelected(previous, selected);
            }
        });
        support.onNodeSelected(previous, selected);
        selectedNode = selected;
    }

    public void onNodeChanged(NodeEvent event, UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        // TODO: Delete files when deleting nodes
        notifyOnValidationErrors(node, nodeChanges);
        nodePagerFragment().onNodeChange(node, nodeChanges);
        support.onNodeChanged(node); // TODO: Only do this if one of the child nodes updated its status or relevance
    }

    private void notifyOnValidationErrors(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        if (!node.equals(selectedNode) && nodeChanges.containsKey(node)) {
            Set<UiValidationError> validationErrors = nodeChanges.get(node).validationErrors;
            if (validationErrors.isEmpty())
                return;
            StringBuilder s = new StringBuilder(node.getLabel() + ":\n");
            for (Iterator<UiValidationError> iterator = validationErrors.iterator(); iterator.hasNext(); ) {
                UiValidationError error = iterator.next();
                s.append(error.toString());
                if (iterator.hasNext())
                    s.append('\n');
            }
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
    }

    public void smartNextAttribute(MenuItem item) {
        List<UiNode> fullNextNodePath = new SmartNext(selectedNode).fullNextNodePath();
        boolean relevanceDependentNodeInPath = false;
        for (UiNode n : fullNextNodePath) {
            if (n.getDefinition().relevanceSources.contains(selectedNode.getDefinition())) {
                relevanceDependentNodeInPath = true;
                break;
            }
        }
        if (relevanceDependentNodeInPath) {
            //TODO trigger node save properly
            nodePagerFragment().prepareNodeDeselect(selectedNode);

            UiNode next = new SmartNext(selectedNode).next();
            performNavigateToSmartNextAttribute(next);

            //wait for record update process to complete
            /*
            final ProgressDialog progressDialog = Dialogs.showProgressDialog(this);

            surveyService.registerRecordUpdateCallback(new Runnable() {
                public void run() {
                    progressDialog.dismiss();
                    //calculate a new next node to navigate to
                    UiNode next = new SmartNext(selectedNode).next();
                    performNavigateToSmartNextAttribute(next);
                }
            });
            nodePagerFragment().prepareNodeDeselect(selectedNode);
            */
        } else {
            UiNode next = fullNextNodePath.get(fullNextNodePath.size() - 1);
            performNavigateToSmartNextAttribute(next);
        }
    }

    private void performNavigateToSmartNextAttribute(final UiNode next) {
        if (next instanceof UiRecordCollection) {
            int confirmTitleKey, confirmMessageKey;
            if (selectedNode.getUiRecord().getStatus() == UiNode.Status.VALIDATION_ERROR) {
                confirmTitleKey = R.string.navigate_to_record_list_with_errors_confirm_title;
                confirmMessageKey = R.string.navigate_to_record_list_with_errors_confirm_message;
            } else {
                confirmTitleKey = R.string.navigate_to_record_list_confirm_title;
                confirmMessageKey = R.string.navigate_to_record_list_confirm_message;
            }
            Dialogs.confirm(this, confirmTitleKey, confirmMessageKey, new Runnable() {
                    public void run() {
                        navigateTo(next);
                    }
                }, null, R.string.go_to_record_list);
        } else {
            if (next.getParent() == selectedNode.getParent()) {
                ViewPager pager = nodePager();
                List<UiNode> relevantSiblings = next.getRelevantSiblings();
                int nextIndex = relevantSiblings.indexOf(next);
                pager.setCurrentItem(nextIndex);
            } else
                navigateTo(next);
        }
    }

    public void nextNode(MenuItem item) {
        if (hasNextSibling()) {
            ViewPager pager = nodePager();
            pager.setCurrentItem(pager.getCurrentItem() + 1);
        }
    }

    public void prevNode(MenuItem item) {
        if (hasPrevSibling()) {
            ViewPager pager = nodePager();
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    private boolean hasNextSibling() {
        ViewPager pager = nodePager();
        int attributeIndex = pager.getCurrentItem();
        return attributeIndex < pager.getAdapter().getCount() - 1;
    }

    private boolean hasPrevSibling() {
        ViewPager pager = nodePager();
        int attributeIndex = pager.getCurrentItem();
        return attributeIndex > 0;
    }

    public void navigateDown(View view) {
        navigateDown();
    }

    public void navigateDown(MenuItem item) {
        navigateDown();
    }

    public void backup(MenuItem item) {
        new Backup(this).execute();
    }

    public void exportDialog(MenuItem item) {
        new ExportDialogFragment().show(getSupportFragmentManager(), "export-dialog");
    }


    private void navigateTo(UiNode node) {
        navigateTo(node.getId());
    }

    public void navigateTo(int nodeId) {
        Keyboard.hide(this);
        Intent intent = createSelectNodeIntent(nodeId);
        startActivity(intent);
    }
    /*
    private void navigateHome() {
        Keyboard.hide(this);
        startActivity(new Intent(this, SurveyNodeActivity.class));
    }

    public void reloadWithoutBackStack() {
        Intent intent = createSelectNodeIntent(selectedNode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    */

    @Override
    public void onResume() {
        if (surveyService != null) {
            ServiceLocator.init(this);
            surveyService.setListener(this);
            if (selectedNode != null) {
                UiRecord uiRecord = selectedNode.getUiRecord();
                selectNode(uiRecord == null ? 0 : uiRecord.getId(), selectedNode.getId());
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (surveyService != null) {
            surveyService.setListener(null);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BackStackLimiter.remove(this);
    }

    private void navigateDown() {
        if (selectedNode instanceof UiInternalNode) {
            UiInternalNode node = (UiInternalNode) selectedNode;
            if (node.getChildCount() == 0)
                return; // TODO: Handle case where tab contains no children
            navigateTo(node.getFirstChild());
        }
    }

    private void navigateUp() {
        UiNode node = surveyService.selectedNode().getParent();
        if (node.getParent() == null)
            return; // Already at the root, cannot navigate up
        if (node.getParent().excludeWhenNavigating())
            node = node.getParent();
        navigateTo(node.getId());
    }

    private LayoutDependentSupport createLayoutSupport() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        UiNode selectedNodeParent = surveyService.selectedNode().getParent();
        boolean showingListOfRecords = selectedNodeParent instanceof UiSurvey;
        this.twoPane = dpWidth >= TWO_PANE_MIN_SCREEN_WIDTH && !showingListOfRecords;
        return twoPane ? new TwoPaneSurveySupport() : new SinglePaneSurveySupport();
    }

    private ViewPager nodePager() {
        return (ViewPager) findViewById(R.id.attributePager);
    }

    private Intent createSelectNodeIntent(int id) {
        Intent intent = new Intent(this, SurveyNodeActivity.class);
        intent.putExtra(ARG_NODE_ID, id);
        return intent;
    }

    private UiNode selectInitialNode(Bundle savedState) {
        if (savedState != null) {
            selectNode(savedState.getInt(ARG_RECORD_ID), savedState.getInt(ARG_NODE_ID));
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(ARG_NODE_ID)) {
                selectNode(extras.getInt(ARG_RECORD_ID), extras.getInt(ARG_NODE_ID));
            }
        }
        return surveyService.selectedNode();
    }

    private void selectNode(int recordId, int nodeId) {
        if (recordId > 0 && !surveyService.isRecordSelected(recordId))
            surveyService.selectRecord(recordId);
        if (nodeId > 0)
            selectedNode = surveyService.selectNode(nodeId);
    }

    private void enableUpNavigationIfNeeded(UiNode node) {
        if (!(node instanceof UiRecordCollection))
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private NodePagerFragment nodePagerFragment() {
        return (NodePagerFragment) getSupportFragmentManager().findFragmentByTag("nodePagerFragment");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_CAPTURE_REQUEST_CODE:
                    if (imageListener != null)
                        imageListener.imageChanged();
                    break;
                case IMAGE_SELECTED_REQUEST_CODE:
                    if (imageListener != null && data != null)
                        imageListener.imageSelected(data.getData());
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setImageChangedListener(FileAttributeComponent listener) {
        imageListener = listener;
    }

    public static void restartActivity(Activity activity) {
        Keyboard.hide(activity);
        Intent intent = new Intent(activity, SurveyNodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    public void showEntityTable(MenuItem menuItem) {
        Keyboard.hide(this);
        EntityTableDialogFragment.show(getSupportFragmentManager());
    }

    public void navigateToSendDataToCollect(MenuItem menuItem) {
        navigateToSendDataToCollect();
    }

    private void navigateToSendDataToCollect() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean remoteSyncEnabled = preferences.getBoolean(SettingsActivity.REMOTE_SYNC_ENABLED, false);
        if (remoteSyncEnabled) {
            Dialogs.confirm(this, R.string.submit_to_collect_confirm_title, R.string.submit_to_collect_confirm_message, new Runnable() {
                public void run() {
                    Keyboard.hide(SurveyNodeActivity.this);
                    SurveyNodeActivity.this.startActivity(new Intent(SurveyNodeActivity.this, SubmitDataToCollectActivity.class));
                }
            });
        } else {
            Toast.makeText(this, R.string.submit_to_collect_remote_sync_not_configured, Toast.LENGTH_SHORT).show();
        }
    }

    private abstract class LayoutDependentSupport {
        abstract void onCreate(Bundle savedState);

        void onNodeSelected(UiNode previous, UiNode selected) { }

        void onNodeChanged(UiNode node) { }
    }

    private class SinglePaneSurveySupport extends LayoutDependentSupport {
        public void onCreate(Bundle savedState) {
            setContentView(R.layout.activity_single_pane_node);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.attribute_detail_pager_container, new NodePagerFragment(), "nodePagerFragment")
                    .commit();
        }
    }

    private class TwoPaneSurveySupport extends LayoutDependentSupport {
        public void onCreate(Bundle savedState) {
            setContentView(R.layout.activity_two_pane_node);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.attribute_detail_pager_container, new NodePagerFragment(), "nodePagerFragment")
                    .commit();
        }

        public void onNodeSelected(UiNode previous, UiNode selected) {
            SimpleNodeListFragment nodeListFragment = listFragment();
            if (nodeListFragment != null)
                setNodeSelected(selected, nodeListFragment);
        }

        void onNodeChanged(UiNode attribute) {
            listFragment().notifyNodeChanged(attribute);
            //listFragment().getListView().invalidateViews();
        }

        private SimpleNodeListFragment listFragment() {
            return (SimpleNodeListFragment) getSupportFragmentManager().findFragmentById(R.id.attribute_list);
        }

        private void setNodeSelected(UiNode selected, SimpleNodeListFragment nodeListFragment) {
            nodeListFragment.selectNode(selected);
        }
    }

    /**
     * Limits the backstack to 10 activities (prevents OutOfMemory errors)
     */
    private static class BackStackLimiter {

        private static final int MAX_QUEUE_SIZE = 10;
        private static final LinkedList<SurveyNodeActivity> queue = new LinkedList<SurveyNodeActivity>();

        private static void enqueue(SurveyNodeActivity activity) {
            if (activity.selectedNode instanceof UiRecordCollection) {
                for (SurveyNodeActivity a: queue) {
                    a.finish();
                }
                queue.clear();
            }
            queue.add(activity);

            if (queue.size() > MAX_QUEUE_SIZE) {
                //do not terminate first 2 activities (record list and selected record activity)
                SurveyNodeActivity toBeTerminated = queue.get(2);
                toBeTerminated.finish();
                queue.remove(toBeTerminated);
            }
        }

        private static void remove(SurveyNodeActivity activity) {
            queue.remove(activity);
        }
    }
}