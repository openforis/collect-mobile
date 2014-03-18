package org.openforis.collect.android.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.pager.NodePagerFragment;
import org.openforis.collect.android.viewmodel.*;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class SurveyNodeActivity extends ActionBarActivity implements SurveyListener, NodeNavigator {
    private static final String ARG_NODE_ID = "node_id";
    private static final String ARG_RECORD_ID = "record_id";

    private NodePagerFragment nodePagerFragment;
    private LayoutDependentSupport support;
    private SurveyService surveyService;

    private UiNode selectedNode;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ServiceLocator.init(getApplicationContext());
        nodePagerFragment = new NodePagerFragment();
        surveyService = ServiceLocator.surveyService();
        support = createLayoutSupport();
        selectedNode = selectInitialNode(savedState); // TODO: Ugly that we have to wait with registering the listener, not to get this callback
        enableUpNavigationIfNeeded(selectedNode);
        surveyService.setListener(this);
        support.onCreate(savedState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.node_activity_actions, menu);
        return true;
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
        nodePagerFragment.getView().post(new Runnable() {
            public void run() {
                nodePagerFragment.onNodeSelected(previous, selected);
            }
        });
        support.onNodeSelected(previous, selected);
        selectedNode = selected;
    }

    public void onAttributeChanged(UiAttribute attribute, Set<UiValidationError> validationErrors) {
        nodePagerFragment.onAttributeChange(attribute);
        if (!validationErrors.isEmpty())
            onValidationError(validationErrors);
        support.onAttributeChanged(attribute);
    }

    private void onValidationError(Set<UiValidationError> validationErrors) {
        // TODO: Implement...
        StringBuilder s = new StringBuilder();
        for (Iterator<UiValidationError> iterator = validationErrors.iterator(); iterator.hasNext(); ) {
            UiValidationError error = iterator.next();
            s.append(error.toString());
            if (iterator.hasNext())
                s.append('\n');
        }
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedParameters")
    public void nextAttribute(MenuItem item) {// TODO: Implement this properly - should not only navigate siblings
        UiNode node = selectedNode;
        ViewPager pager = nodePager();
        int attributeIndex = pager.getCurrentItem();
        if (attributeIndex < node.getSiblingCount()) {
            pager.setCurrentItem(attributeIndex + 1);
        } else
            System.out.println("Should navigate to next node");
    }

    @SuppressWarnings("UnusedParameters")
    public void navigateDown(View view) {
        navigateDown();
    }

    @SuppressWarnings("UnusedParameters")
    public void navigateDown(MenuItem item) {
        navigateDown();
    }

    public void navigateTo(int nodeId) {
        Intent intent = createSelectNodeIntent(nodeId);
        startActivity(intent);
    }

    public void onResume() {
        ServiceLocator.init(getApplicationContext());
        surveyService.setListener(this);
        UiRecord uiRecord = selectedNode.getUiRecord();
        selectNode(uiRecord == null ? 0 : uiRecord.getId(), selectedNode.getId());
        super.onResume();
    }

    protected void onPause() {
        surveyService.setListener(null);
        super.onPause();
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (!(selectedNode instanceof UiRecordCollection)) // TODO: Ugly
            outState.putInt(ARG_RECORD_ID, selectedNode.getUiRecord().getId());
        outState.putInt(ARG_NODE_ID, selectedNode.getId());
        super.onSaveInstanceState(outState);
    }

    private void navigateDown() {
        if (selectedNode instanceof UiInternalNode) {
            startActivity(
                    createSelectNodeIntent(((UiInternalNode) selectedNode).getFirstChild())
            );
        }
    }

    private void navigateUp() {
        UiNode node = surveyService.selectedNode().getParent();
        if (node.getParent().excludeWhenNavigating())
            node = node.getParent();
        navigateTo(node.getId());
    }

    private LayoutDependentSupport createLayoutSupport() {
        return getResources().getBoolean(R.bool.twoPane)
                ? new TwoPaneSurveySupport()
                : new SinglePaneSurveySupport();
    }

    private ViewPager nodePager() {
        return (ViewPager) findViewById(R.id.attributePager);
    }

    private Intent createSelectNodeIntent(UiNode node) {
        return createSelectNodeIntent(node.getId());
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

    private abstract class  LayoutDependentSupport {
        abstract void onCreate(Bundle savedState);

        void onNodeSelected(UiNode previous, UiNode selected) {

        }

        void onAttributeChanged(UiAttribute attribute) {

        }
    }

    private class SinglePaneSurveySupport extends LayoutDependentSupport {
        public void onCreate(Bundle savedState) {
            setContentView(R.layout.activity_single_pane_node);

            // savedState is non-null when there is fragment state
            // saved from previous configurations of this activity
            // (e.g. when rotating the screen from portrait to landscape).
            // In this case, the fragment will automatically be re-added
            // to its container so we don't need to manually add it.
            if (savedState == null)
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.attribute_detail_pager_container, nodePagerFragment)
                        .commit();
        }
    }

    private class TwoPaneSurveySupport extends LayoutDependentSupport {
        public void onCreate(Bundle savedState) {
            setContentView(R.layout.activity_two_pane_node);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.attribute_detail_pager_container, nodePagerFragment)
                    .commit();
        }

        public void onNodeSelected(UiNode previous, UiNode selected) {
            final ListFragment nodeListFragment = listFragment();
            if (nodeListFragment != null)
                setNodeSelected(selected, nodeListFragment);
        }

        void onAttributeChanged(UiAttribute attribute) {
            listFragment().getListView().invalidateViews();
        }

        private ListFragment listFragment() {
            return (ListFragment) getSupportFragmentManager().findFragmentById(R.id.attribute_list);
        }

        private void setNodeSelected(UiNode selected, ListFragment nodeListFragment) {
            ListView listView = nodeListFragment.getListView();
            int i = selected.getIndexInParent();
            listView.setItemChecked(i, true);
            listView.smoothScrollToPosition(i);
        }
    }
}