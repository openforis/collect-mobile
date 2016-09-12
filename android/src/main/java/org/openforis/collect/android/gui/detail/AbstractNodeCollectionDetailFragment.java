package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.list.EntityListAdapter;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;

import java.util.Map;

/**
 * @author Daniel Wiell
 */
public abstract class AbstractNodeCollectionDetailFragment<T extends UiInternalNode> extends NodeDetailFragment<T> {

    private EntityListAdapter adapter;

    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_entity_collection_detail, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View addEntity = view.findViewById(R.id.action_add_node);
        if (addEntity != null) {
            addEntity.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    UiInternalNode node = addNode();
                    nodeNavigator().navigateTo(node.getFirstChild().getId());
                }
            });
        }
    }

    public void onNodeChange(UiNode node, Map<UiNode, UiNodeChange> nodeChanges) {
        super.onNodeChange(node, nodeChanges);
        boolean changedChildNode = node.getParent().equals(node());  // TODO: If removed, can we rely on parent to be present?
        if (changedChildNode || nodeChanges.containsKey(node()))
            adapter.notifyDataSetChanged();
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
        ListView listView = listView(rootView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                T nodeCollection = node();
                UiInternalNode selectedNode = getSelectedNode(position, nodeCollection);
                nodeNavigator().navigateTo(selectedNode.getFirstChild().getId());
            }
        });
    }

    private ListView listView(View rootView) {
        return (ListView) rootView.findViewById(R.id.entity_list);
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }
}
