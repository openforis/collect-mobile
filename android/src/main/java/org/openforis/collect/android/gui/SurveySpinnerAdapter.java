package org.openforis.collect.android.gui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openforis.collect.R;

/**
 * @author Stefano Ricci
 */
public class SurveySpinnerAdapter extends SurveyBaseAdapter {

    public SurveySpinnerAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, getItemLayout());
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, getDropDownItemLayout());
    }

    @NonNull
    public View getView(int position, View convertView, ViewGroup parent, int itemLayout) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = (TextView) inflater.inflate(itemLayout, parent, false);
        }
        String text;
        if (isImportSurveyItem(position)) {
            text = activity.getString(R.string.import_new_survey_list_item);
        } else {
            SurveyItem survey = surveys.get(position);
            text = survey.name;
        }
        ((TextView) view).setText(text);
        return view;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.spinner_item;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1; //include "Import survey" item
    }

    public boolean isSurveyItem(int itemPosition) {
        return itemPosition < getCount() - 1;
    }

    public boolean isImportSurveyItem(int itemPosition) {
        return itemPosition == getCount() - 1;
    }

    private int getDropDownItemLayout() {
        return R.layout.spinner_dropdown_item;
    }
}
