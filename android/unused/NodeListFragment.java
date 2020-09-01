package org.openforis.collect.android.gui.list;

import android.os.Bundle;
import androidx.core.app.ListFragment;
import androidx.core.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.viewmodel.UiNode;

/**
 * @author Daniel Wiell
 */
public class NodeListFragment extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new NodeListAdapter(getActivity(), node().getParent()));
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

    private UiNode node() {
        return ServiceLocator.surveyService().selectedNode();
    }

    private void onAttributeSelected(int attributeIndex) {
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.attributePager);
        viewPager.setCurrentItem(attributeIndex);
    }
}
