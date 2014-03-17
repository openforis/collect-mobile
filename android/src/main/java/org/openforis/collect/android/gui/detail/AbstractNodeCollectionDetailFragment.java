package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public abstract class AbstractNodeCollectionDetailFragment<T extends UiInternalNode> extends NodeDetailFragment<T> {
    private static final int MAX_ATTRIBUTES = 5;
    private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 50;

    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_entity_collection_detail, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View addEntity = view.findViewById(R.id.action_add_entity);
        if (addEntity != null) {
            addEntity.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    UiInternalNode node = addNode();
                    nodeNavigator().navigateTo(node.getFirstChild().getId());
                }
            });
        }
    }

    public void onResume() {
        super.onResume();
        setupNodeCollection(getView());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.entity_collection_details_fragment_actions, menu);
    }

    protected abstract UiInternalNode addNode();

    protected abstract UiInternalNode getSelectedNode(int position, T nodeCollection);

    protected SurveyService surveyService() {
        return ServiceLocator.surveyService();
    }

    private void setupNodeCollection(View rootView) {
        ListView nodeListView = (ListView) rootView.findViewById(R.id.entity_list);
        int layout = AndroidVersion.greaterThen10() // TODO: Use style instead
                ? android.R.layout.simple_list_item_activated_1
                : android.R.layout.simple_list_item_1;
        nodeListView.setAdapter(new ArrayAdapter<String>(
                getActivity(),
                layout,
                android.R.id.text1,
                nodeListItems()));
        nodeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                T nodeCollection = node();
                UiInternalNode selectedNode = getSelectedNode(position, nodeCollection);
                nodeNavigator().navigateTo(selectedNode.getFirstChild().getId());
            }
        });
    }

    private List<String> nodeListItems() {
        List<String> items = new ArrayList<String>();
        T nodeCollection = node();
        List<UiNode> children = nodeCollection.getChildren();
        for (int i = 0; i < children.size(); i++) {
            UiNode child = children.get(i);
            // TODO: Should assemble the attribute name/value manually,
            // to so "Unspecified" can be picked up from a resource, and different parts can be styled separately
            List<UiAttribute> attributes = getKeyAttributes(i, child);
            if (attributes.isEmpty())
                attributes = allChildAttributes((UiInternalNode) child);
            items.add(toNodeLabel(attributes));
        }
        return items;
    }

    private String toNodeLabel(List<UiAttribute> attributes) {
        StringBuilder s = new StringBuilder();
        for (Iterator<UiAttribute> iterator = attributes.iterator(); iterator.hasNext(); ) {
            String attribute = iterator.next().toString();
            s.append(attribute.substring(0, Math.min(attribute.length(), MAX_ATTRIBUTE_VALUE_LENGTH)));
            if (iterator.hasNext())
                s.append('\n');
        }
        return s.toString();
    }

    private List<UiAttribute> allChildAttributes(UiInternalNode node) {
        List<UiAttribute> attributes = new ArrayList<UiAttribute>();
        for (UiNode potentialAttribute : node.getChildren()) {
            if (potentialAttribute instanceof UiAttribute)
                attributes.add((UiAttribute) potentialAttribute);
            if (attributes.size() > MAX_ATTRIBUTES)
                return attributes;
        }
        return attributes;
    }

    private List<UiAttribute> getKeyAttributes(int i, UiNode child) {
        if (child instanceof UiEntity)
            return ((UiEntity) child).getKeyAttributes();
        if (child instanceof UiRecord.Placeholder)
            return ((UiRecord.Placeholder) child).getKeyAttributes();
        throw new IllegalStateException("Unexpected node type. Expected UiEntity or UiRecord.Placeholder, was " + child.getClass());
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }
}
