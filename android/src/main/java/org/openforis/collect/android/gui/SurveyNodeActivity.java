package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import org.openforis.collect.android.gui.util.WorkingDir;
import org.openforis.collect.android.viewmodel.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class SurveyNodeActivity extends ActionBarActivity implements SurveyListener, NodeNavigator {
    private static final int IMPORT_SURVEY_REQUEST_CODE = 6384; // onActivityResult request

    private static final String ARG_NODE_ID = "node_id";
    private static final String ARG_RECORD_ID = "record_id";

    private LayoutDependentSupport support;
    private SurveyService surveyService;

    private UiNode selectedNode;

    public void onCreate(Bundle savedState) {
        try {
            if (ServiceLocator.init(this)) {
                ThemeInitializer.init(this);
                super.onCreate(savedState);
                surveyService = ServiceLocator.surveyService();
                support = createLayoutSupport();
                selectedNode = selectInitialNode(savedState); // TODO: Ugly that we have to wait with registering the listener, not to get this callback
                enableUpNavigationIfNeeded(selectedNode);
                surveyService.setListener(this);
                support.onCreate(savedState);
            } else {
                super.onCreate(savedState);
                DialogFragment newFragment = new ImportingDemoSurveyDialog();
                newFragment.show(getSupportFragmentManager(), "importingDemoSurvey");
            }
        } catch (WorkingDirNotWritable ignore) {
            super.onCreate(savedState);
            DialogFragment newFragment = new SecondaryStorageNotFoundFragment();
            newFragment.show(getSupportFragmentManager(), "secondaryStorageNotFound");
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
                NodePagerFragment nodePagerFragment = nodePagerFragment();
                if (nodePagerFragment != null)
                    nodePagerFragment.onNodeSelected(previous, selected);
            }
        });
        support.onNodeSelected(previous, selected);
        selectedNode = selected;
    }

    public void onNodeChanged(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        notifyOnValidationErrors(node, nodeChanges);
        nodePagerFragment().onNodeChange(node, nodeChanges);
        support.onNodeChanged(node); // TODO: Only do this if one of the child nodes updated it's status or relevance
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

    public void backup(MenuItem item) {
        new Backup(this).execute();
    }

    public void export(MenuItem item) {
        try {
            surveyService.exportSurvey();
            String message = getResources().getString(R.string.toast_exported_survey, WorkingDir.root(this));
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            String message = getResources().getString(R.string.toast_exported_survey_failed);
            Log.e("export", message, e);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void navigateTo(int nodeId) {
        Intent intent = createSelectNodeIntent(nodeId);
        startActivity(intent);
    }

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

    public void surveyImportRequested(MenuItem item) {
        surveyImportRequested();
    }

    private void surveyImportRequested() {
        if (WorkingDir.databases(this).exists()) {
            DialogFragment dialogFragment = new ImportOverwriteDataConfirmation();
            dialogFragment.show(getSupportFragmentManager(), "confirmDataDeletionAndImport");
        } else {
            showImportDialog();
        }

    }

    protected void showImportDialog() {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(
                target, "Select survey to import");
        startActivityForResult(intent, IMPORT_SURVEY_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMPORT_SURVEY_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null)
                    importSurvey(data.getData());
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void importSurvey(Uri surveyUri) {
        final String path = FileUtils.getPath(this, surveyUri);
        String message = getResources().getString(R.string.toast_import_survey);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        try {
            ServiceLocator.importSurvey(path, this);
            selectedNode = null;
            startImportedSurveyNodeActivity();
        } catch (MalformedSurvey malformedSurvey) {
            importFailedDialog(
                    malformedSurvey.sourceName,
                    getString(R.string.import_text_failed)
            );
        } catch (WrongSurveyVersion wrongSurveyVersion) {
            importFailedDialog(
                    wrongSurveyVersion.sourceName,
                    getString(R.string.import_text_wrong_version)
            );
        }
    }

    public void startImportedSurveyNodeActivity() {
        Intent intent = new Intent(this, SurveyNodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void importFailedDialog(String surveyPath, String message) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.import_title_failed, surveyPath))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showImportDialog();
                    }
                })
                .show();
    }

    public static void restartActivity(Context context) {
        Intent intent = new Intent(context, SurveyNodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private abstract class LayoutDependentSupport {
        abstract void onCreate(Bundle savedState);

        void onNodeSelected(UiNode previous, UiNode selected) {

        }

        void onNodeChanged(UiNode node) {

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

        void onNodeChanged(UiNode attribute) {
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