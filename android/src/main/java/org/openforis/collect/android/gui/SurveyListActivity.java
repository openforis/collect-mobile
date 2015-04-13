package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Attrs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurveyListActivity extends ActionBarActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_list);

        ListView listView = (ListView) findViewById(R.id.survey_list);

        final SurveyAdapter adapter = new SurveyAdapter(this, surveys());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String surveyName = adapter.survey(position).name;
                SurveyImporter.selectSurvey(surveyName, SurveyListActivity.this);
                startActivity(new Intent(SurveyListActivity.this, SurveyNodeActivity.class));
            }
        });
        adapter.setSurveysDeletedListener(new SurveyAdapter.SurveysDeletedListener() {
            public void onSurveysDeleted(Set<Survey> surveys) {
                System.out.println("Deleted " + surveys);
            }
        });
        selectSurvey(listView, adapter);

    }

    private void selectSurvey(ListView listView, SurveyAdapter adapter) {
        String selectedSurvey = SurveyImporter.selectedSurvey(this);
        if (selectedSurvey != null) {
            int position = adapter.position(selectedSurvey);
            if (position >= 0) {
                listView.setSelection(position);
                listView.setItemChecked(position, true);
            }
        }
    }

    private List<Survey> surveys() {
        List<Survey> surveys = new ArrayList<Survey>();
        File surveysRootDir = AppDirs.surveysRootDir(this);
        for (File databaseDir : surveysRootDir.listFiles()) {
            if (databaseDir.isDirectory())
                surveys.add(createSurvey(databaseDir));
        }
        return surveys;
    }

    private Survey createSurvey(File databaseDir) {
        return new Survey(databaseDir.getName(), databaseDir.getName());
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

    public static class SurveyAdapter extends BaseAdapter {
        private final Activity activity;
        private List<Survey> surveys;
        private final Attrs attrs;

        private final Set<Survey> surveysToEdit = new HashSet<Survey>();
        private final Set<CheckBox> checked = new HashSet<CheckBox>();
        private ActionMode actionMode;
        private SurveysDeletedListener surveysDeletedListener;

        public SurveyAdapter(Activity activity, List<Survey> surveys) {
            this.activity = activity;
            this.surveys = new ArrayList<Survey>(surveys);
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
                        surveysToEdit.add(survey);
                        checked.add(checkbox);
                    } else {
                        surveysToEdit.remove(survey);
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

        public Survey survey(int position) {
            return surveys.get(position);
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
            surveys.removeAll(surveysToEdit);
            actionMode.finish();
            notifyDataSetChanged();
        }

        private static class SurveyHolder {
            TextView text;
        }

        public interface SurveysDeletedListener {
            void onSurveysDeleted(Set<Survey> surveys);
        }
    }
}
