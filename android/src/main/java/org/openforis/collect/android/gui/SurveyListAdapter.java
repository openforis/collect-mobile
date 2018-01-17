package org.openforis.collect.android.gui;

import android.app.Activity;
import android.view.*;
import android.widget.CheckBox;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyBaseAdapter;

import java.util.*;

public class SurveyListAdapter extends SurveyBaseAdapter {

    private final Set<String> surveysToEdit = new HashSet<String>();
    private final Set<CheckBox> checked = new HashSet<CheckBox>();
    private ActionMode actionMode;
    private SurveysDeletedListener surveysDeletedListener;

    public SurveyListAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        SurveyItem survey = surveys.get(position);
        setupEditActions(survey, view);
        return view;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.listview_survey;
    }

    private void setupEditActions(final SurveyItem survey, View row) {
        final CheckBox checkbox = (CheckBox) row.findViewById(R.id.surveySelectedForAction);
        checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkbox.isChecked()) {
                    surveysToEdit.add(survey.name);
                    checked.add(checkbox);
                } else {
                    surveysToEdit.remove(survey.name);
                    checked.remove(checkbox);
                }

                if (!surveysToEdit.isEmpty()) {
                    if (actionMode == null)
                        actionMode = activity.startActionMode(new EditCallback());
                    else
                        setEditTitle(actionMode);
                }
                if (surveysToEdit.isEmpty() && actionMode != null)
                    actionMode.finish();
            }
        });
    }

    private void setEditTitle(ActionMode mode) {
        mode.setTitle(activity.getString(R.string.amount_selected, surveysToEdit.size()));
    }

    public void setSurveysDeletedListener(SurveysDeletedListener surveysDeletedListener) {
        this.surveysDeletedListener = surveysDeletedListener;
    }

    public String survey(int position) {
        return surveys.get(position).name;
    }

    public int position(String selectedSurvey) {
        if (selectedSurvey == null)
            return -1;
        for (int i = 0; i < surveys.size(); i++) {
            SurveyItem survey = surveys.get(i);
            if (survey.label.equals(selectedSurvey))
                return i;
        }
        return -1;
    }

    private class EditCallback implements ActionMode.Callback {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.entity_action_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            setEditTitle(mode);
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_selected_nodes:
                    deleteCheckedSurveys();
                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            surveysToEdit.clear();
            for (CheckBox checkBox : checked) {
                checkBox.setChecked(false);
                checkBox.setSelected(false);
            }
        }
    }

    private void deleteCheckedSurveys() {
        if (surveysDeletedListener != null)
            surveysDeletedListener.onSurveysDeleted(surveysToEdit);
        for (Iterator<SurveyItem> iterator = surveys.iterator(); iterator.hasNext(); ) {
            SurveyItem survey = iterator.next();
            if (surveysToEdit.contains(survey.name))
                iterator.remove();
        }

        actionMode.finish();
        notifyDataSetChanged();
    }

    public interface SurveysDeletedListener {
        void onSurveysDeleted(Set<String> surveys);
    }
}
