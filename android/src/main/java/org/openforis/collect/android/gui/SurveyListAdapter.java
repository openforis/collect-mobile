package org.openforis.collect.android.gui;

import android.app.Activity;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Attrs;

import java.io.File;
import java.util.*;

public class SurveyListAdapter extends BaseAdapter {
    private final Activity activity;
    private List<Survey> surveys;
    private final Attrs attrs;

    private final Set<String> surveysToEdit = new HashSet<String>();
    private final Set<CheckBox> checked = new HashSet<CheckBox>();
    private ActionMode actionMode;
    private SurveysDeletedListener surveysDeletedListener;

    public SurveyListAdapter(Activity activity) {
        this.activity = activity;
        this.surveys = surveys();
        attrs = new Attrs(activity);
    }

    public int getCount() {
        return surveys.size();
    }

    public Object getItem(int position) {
        return surveys.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private List<Survey> surveys() {
        List<Survey> surveys = new ArrayList<Survey>();
        File surveysRootDir = AppDirs.surveysDir(activity);
        if (!surveysRootDir.exists())
            surveysRootDir.mkdirs();
        for (File databaseDir : surveysRootDir.listFiles())
            if (databaseDir.isDirectory())
                surveys.add(new Survey(databaseDir.getName(), databaseDir.getName()));
        return surveys;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SurveyHolder holder;
        if (row == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.listview_survey, parent, false);
            if (AndroidVersion.greaterThan10())
                setBackground(row);

            holder = new SurveyHolder();
            holder.text = (TextView) row.findViewById(R.id.surveyLabel);
            row.setTag(holder);
        } else {
            holder = (SurveyHolder) row.getTag();
        }

        Survey survey = surveys.get(position);
        holder.text.setText(survey.label);
        holder.text.setTextColor(attrs.color(R.attr.relevantTextColor)); // TODO: Needed?
        setupEditActions(survey, row);

        return row;
    }

    private void setupEditActions(final Survey survey, View row) {
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

    private void setBackground(View row) {
        row.setBackgroundResource(attrs.resourceId(android.R.attr.activatedBackgroundIndicator));
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
            Survey survey = surveys.get(i);
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
        for (Iterator<Survey> iterator = surveys.iterator(); iterator.hasNext(); ) {
            Survey survey = iterator.next();
            if (surveysToEdit.contains(survey.name))
                iterator.remove();
        }

        actionMode.finish();
        notifyDataSetChanged();
    }

    private static class SurveyHolder {
        TextView text;
    }


    private static class Survey {
        public final String name;
        public final String label;

        public Survey(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public String toString() {
            return "Survey{" +
                    "name='" + name + '\'' +
                    ", label='" + label + '\'' +
                    '}';
        }
    }

    public interface SurveysDeletedListener {
        void onSurveysDeleted(Set<String> surveys);
    }
}
