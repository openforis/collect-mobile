package org.openforis.collect.android.gui.detail;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import de.timroes.android.listview.EnhancedListView;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.NodeNavigator;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.list.EntityListAdapter;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;

/**
 * @author Daniel Wiell
 */
public abstract class AbstractNodeCollectionDetailFragment<T extends UiInternalNode> extends NodeDetailFragment<T> {

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

    public void onPause() {
        super.onPause();
        listView(getView()).discardUndo(); // Prevent node to be re-inserted
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

    protected abstract void removeNode(UiNode node);

    protected SurveyService surveyService() {
        return ServiceLocator.surveyService();
    }

    private void setupNodeCollection(View rootView) {
        final EntityListAdapter adapter = new EntityListAdapter(getActivity(), node());
        EnhancedListView listView = listView(rootView);
        listView.setUndoStyle(EnhancedListView.UndoStyle.MULTILEVEL_POPUP);
        listView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, final int position) {
                final UiNode node = adapter.getItem(position);
                adapter.remove(position);
                return new EnhancedListView.Undoable() {
                    public void undo() {
                        adapter.insert(position, node);
                    }

                    public void discard() {
                        removeNode(node);
                    }

                    public String getTitle() {
                        return adapter.getText(node) + " deleted";
                    }
                };
            }
        });
        listView.enableSwipeToDismiss();
        listView.setSwipeDirection(EnhancedListView.SwipeDirection.END);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                T nodeCollection = node();
                UiInternalNode selectedNode = getSelectedNode(position, nodeCollection);
                nodeNavigator().navigateTo(selectedNode.getFirstChild().getId());
            }
        });
    }

    private EnhancedListView listView(View rootView) {
        return (EnhancedListView) rootView.findViewById(R.id.entity_list);
    }

    private NodeNavigator nodeNavigator() {
        return (NodeNavigator) getActivity();
    }
}
