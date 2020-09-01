package org.openforis.collect.android.gui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.commons.collection.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefano Ricci
 */
public abstract class SurveyBaseAdapter extends BaseAdapter {

    protected final Activity activity;
    protected List<SurveyItem> surveys;
    protected final Attrs attrs;

    public SurveyBaseAdapter(Activity activity) {
        this.activity = activity;
        this.surveys = surveys();
        attrs = new Attrs(activity);
    }

    public void reloadSurveys() {
        this.surveys = surveys();
        super.notifyDataSetChanged();
    }

    private List<SurveyItem> surveys() {
        List<SurveyItem> surveys = new ArrayList<SurveyItem>();
        File surveysRootDir = AppDirs.surveysDir(activity);
        if (!surveysRootDir.exists())
            surveysRootDir.mkdirs();
        for (File databaseDir : surveysRootDir.listFiles())
            if (databaseDir.isDirectory())
                surveys.add(new SurveyItem(databaseDir.getName(), databaseDir.getName()));
        return surveys;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SurveyHolder holder;
        if (row == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(getItemLayout(), parent, false);
            if (AndroidVersion.greaterThan10())
                setBackground(row);

            holder = new SurveyHolder();
            holder.text = (TextView) row.findViewById(R.id.surveyLabel);
            row.setTag(holder);
        } else {
            holder = (SurveyHolder) row.getTag();
        }

        SurveyItem survey = surveys.get(position);
        holder.text.setText(survey.label);
        holder.text.setTextColor(attrs.color(R.attr.relevantTextColor)); // TODO: Needed?

        return row;
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

    protected int getItemLayout() {
        return R.layout.listview_survey;
    }

    private void setBackground(View row) {
        row.setBackgroundResource(attrs.resourceId(android.R.attr.activatedBackgroundIndicator));
    }

    public int getItemPosition(String surveyName) {
        if (surveyName == null) {
            return -1;
        } else {
            SurveyItem item = CollectionUtils.findItem(surveys, surveyName, "name");
            return surveys.indexOf(item);
        }
    }

    public boolean isSurveyListEmpty() {
        return surveys.isEmpty();
    }

    protected static class SurveyHolder {
        TextView text;
    }

    public static class SurveyItem {
        public final String name;
        public final String label;

        public SurveyItem(String name, String label) {
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
}
