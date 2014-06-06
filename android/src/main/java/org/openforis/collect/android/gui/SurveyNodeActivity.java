package org.openforis.collect.android.gui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.ipaulpro.afilechooser.utils.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyListener;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.pager.NodePagerFragment;
import org.openforis.collect.android.viewmodel.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class SurveyNodeActivity extends ActionBarActivity implements SurveyListener, NodeNavigator {
    private static final int SELECT_SURVEY_REQUEST_CODE = 6384; // onActivityResult request

    private static final String ARG_NODE_ID = "node_id";
    private static final String ARG_RECORD_ID = "record_id";

    private LayoutDependentSupport support;
    private SurveyService surveyService;

    private UiNode selectedNode;

    public void onCreate(Bundle savedState) {
        if (ServiceLocator.init(getApplicationContext())) {
            ThemeInitializer.init(this);
            super.onCreate(savedState);
            surveyService = ServiceLocator.surveyService();
            support = createLayoutSupport();
            selectedNode = selectInitialNode(savedState); // TODO: Ugly that we have to wait with registering the listener, not to get this callback
            enableUpNavigationIfNeeded(selectedNode);
            surveyService.setListener(this);
            support.onCreate(savedState);
        } else {
            Toast.makeText(this, "Select survey to import.", Toast.LENGTH_SHORT).show();
            super.onCreate(savedState);
            showSurveyFileChooser();
        }
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
        } else if (id == R.id.action_settings) {
            settings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void settings() {
        Class activityClass = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                ? SettingsPreHoneycombActivity.class
                : SettingsActivity.class;
        startActivity(new Intent(this, activityClass));
    }

    public void onNodeSelected(final UiNode previous, final UiNode selected) {
        nodePagerFragment().getView().post(new Runnable() {
            public void run() {
                nodePagerFragment().onNodeSelected(previous, selected);
            }
        });
        support.onNodeSelected(previous, selected);
        selectedNode = selected;
    }

    public void onAttributeChanged(UiAttribute attribute, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        notifyOnValidationErrors(attribute, attributeChanges);
        nodePagerFragment().onAttributeChange(attribute, attributeChanges);
        support.onAttributeChanged(attribute);
    }

    private void notifyOnValidationErrors(UiAttribute attribute, Map<UiAttribute, UiAttributeChange> attributeChanges) {
        if (!attribute.equals(selectedNode) && attributeChanges.containsKey(attribute)) {
            Set<UiValidationError> validationErrors = attributeChanges.get(attribute).validationErrors;
            if (validationErrors.isEmpty())
                return;
            StringBuilder s = new StringBuilder(attribute.getLabel() + ":\n");
            for (Iterator<UiValidationError> iterator = validationErrors.iterator(); iterator.hasNext(); ) {
                UiValidationError error = iterator.next();
                s.append(error.toString());
                if (iterator.hasNext())
                    s.append('\n');
            }
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
    }

    public void nextAttribute(MenuItem item) {// TODO: Implement this properly - should not only navigate siblings
        UiNode node = selectedNode;
        ViewPager pager = nodePager();
        int attributeIndex = pager.getCurrentItem();
        if (attributeIndex < node.getSiblingCount()) {
            pager.setCurrentItem(attributeIndex + 1);
        } else
            System.out.println("Should navigate to next node");
    }

    public void navigateDown(View view) {
        navigateDown();
    }

    public void navigateDown(MenuItem item) {
        navigateDown();
    }

    public void export(MenuItem item) {
        // TODO: Show progress bar - so we need some callback to know how far we got...

        try {
            surveyService.exportSurvey();
        } catch (IOException e) {
            Log.e("export", "Failed  to export", e);
            Toast.makeText(this, "Failed to export survey: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void navigateTo(int nodeId) {
        Intent intent = createSelectNodeIntent(nodeId);
        startActivity(intent);
    }

    public void onResume() {
        if (surveyService != null) {
            ServiceLocator.init(getApplicationContext());
            surveyService.setListener(this);
            UiRecord uiRecord = selectedNode.getUiRecord();
            selectNode(uiRecord == null ? 0 : uiRecord.getId(), selectedNode.getId());
        }
        super.onResume();
    }

    protected void onPause() {
        if (surveyService != null) {
            surveyService.setListener(null);
        }
        super.onPause();
    }

    private void navigateDown() {
        if (selectedNode instanceof UiInternalNode) {
            UiInternalNode node = (UiInternalNode) selectedNode;
            if (node.getChildCount() == 0)
                return; // TODO: Handle case where tab contains no children
            startActivity(
                    createSelectNodeIntent(node.getFirstChild())
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

    private NodePagerFragment nodePagerFragment() {
        return (NodePagerFragment) getSupportFragmentManager().findFragmentByTag("nodePagerFragment");
    }

    public void showFileChooser(MenuItem item) {
        showSurveyFileChooser();
    }

    private void showSurveyFileChooser() {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(
                target, "Select survey to import");
        startActivityForResult(intent, SELECT_SURVEY_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_SURVEY_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null)
                    importSurvey(data.getData());
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importSurvey(Uri surveyUri) {
        try {
            // Get the file path from the URI
            final String path = FileUtils.getPath(this, surveyUri);
            Toast.makeText(this, "Importing survey: " + path, Toast.LENGTH_LONG).show();
            ServiceLocator.importSurvey(path, this); // TODO: This should fail if invalid db file
            Intent intent = new Intent(this, SurveyNodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            showSurveyFileChooser();
        }
    }

    private abstract class LayoutDependentSupport {
        abstract void onCreate(Bundle savedState);

        void onNodeSelected(UiNode previous, UiNode selected) {

        }

        void onAttributeChanged(UiAttribute attribute) {

        }
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