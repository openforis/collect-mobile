package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.UiNode;

import java.util.List;

/**
 * @author Daniel Wiell
 */
public class NodeListFragment extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = AndroidVersion.greaterThen10()
                ? android.R.layout.simple_list_item_activated_1
                : android.R.layout.simple_list_item_1;
        setListAdapter(new ArrayAdapter<String>(
                getActivity(),
                layout,
                android.R.id.text1,
                attributeLabels()));
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAttributeSelected(position);
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        int position = node().getIndexInParent();
        setSelection(position);
        getListView().setItemChecked(position, true);
    }

    private List<String> attributeLabels() {
        return node().getSiblingLabels();
    }

    private UiNode node() {
        return ServiceLocator.surveyService().selectedNode();
    }

    private void onAttributeSelected(int attributeIndex) {
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(attributeIndex);
    }
}
