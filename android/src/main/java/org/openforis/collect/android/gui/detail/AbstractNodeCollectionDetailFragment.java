package org.openforis.collect.android.gui.detail;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.EntityListAdapter;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiEntityCollectionDefinition;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Daniel Wiell
 */
public abstract class AbstractNodeCollectionDetailFragment<T extends UiInternalNode> extends NodeDetailFragment<T> {

    private EntityListAdapter adapter;
    private static final Typeface HEADER_TYPEFACE = Typeface.DEFAULT_BOLD;
    private Timer adapterUpdateTimer;

    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_entity_collection_detail, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View addButton = view.findViewById(R.id.action_add_node);
        if (addButton != null) {
            if (isEnumeratedEntityCollection()) {
                Views.hide(addButton);
            } else {
                addButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UiInternalNode node = addNode();
                        nodeNavigator().navigateTo(node.getFirstChild().getId());
                    }
                });
            }
        }
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        super.onNodeChange(node, nodeChanges);
        boolean changedChildNode = node.getParent().equals(node());  // TODO: If removed, can we rely on parent to be present?
        if (changedChildNode || nodeChanges.containsKey(node()))
            adapter.notifyDataSetChanged();
    }

    public void onPause() {
        super.onPause();
        if (adapterUpdateTimer != null)
            adapterUpdateTimer.cancel();
    }

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

    private void setupNodeCollection(View rootView) {
        adapter = new EntityListAdapter((SurveyNodeActivity) getActivity(), this instanceof RecordCollectionDetailFragment, node());

        boolean headerVisible = ! node().getChildren().isEmpty();
        Views.toggleVisibility(rootView, R.id.entity_list_header_wrapper, headerVisible);
        if (headerVisible) {
            buildDynamicHeaderPart(rootView);
            if (isEnumeratedEntityCollection()) {
                Views.hide(rootView, R.id.entity_list_header_selection_checkbox);
            }
        }

        ListView listView = (ListView) rootView.findViewById(R.id.entity_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                T nodeCollection = node();
                UiInternalNode selectedNode = getSelectedNode(position, nodeCollection);
                nodeNavigator().navigateTo(selectedNode.getFirstChild().getId());
            }
        });

        adapterUpdateTimer = new Timer();
        adapterUpdateTimer.schedule(new AdapterUpdaterTask(), 60000, 60000);
    }

    private void buildDynamicHeaderPart(final View rootView) {
        final LinearLayout header = (LinearLayout) rootView.findViewById(R.id.entity_list_header);
        header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int maxWidth = header.getMeasuredWidth();
                List<String> headings = getDynamicHeadings();
                if (! headings.isEmpty()) {
                    int maxWidthPerColumn = Double.valueOf(Math.floor(maxWidth / headings.size())).intValue();
                    for (String heading : headings) {
                        TextView textView = new TextView(getContext());
                        textView.setWidth(maxWidthPerColumn);
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
        });
    }

    private List<String> getDynamicHeadings() {
        List<String> headings = new ArrayList<String>();
        T node = node();
        if (! node.getChildren().isEmpty()) {
            UiNode firstChild = node.getChildren().get(0);
            List<UiAttribute> keyAttributes;
            if (firstChild instanceof UiRecord.Placeholder) {
                keyAttributes = ((UiRecord.Placeholder) firstChild).getKeyAttributes();
            } else {
                keyAttributes = ((UiEntity) firstChild).getKeyAttributes();
            }
            for (UiAttribute key : keyAttributes) {
                TextView keyTextView = new TextView(getContext());
                String heading = key.getDefinition().label;
                headings.add(heading);
            }
        }
        return headings;
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }

    private boolean isEnumeratedEntityCollection() {
        UiNode selectedNode = surveyService().selectedNode();
        Definition nodeDef = selectedNode.getDefinition();
        return nodeDef instanceof UiEntityCollectionDefinition && ((UiEntityCollectionDefinition) nodeDef).isEnumerated();
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
}
