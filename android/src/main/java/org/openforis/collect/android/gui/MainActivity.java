package org.openforis.collect.android.gui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyBaseAdapter;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.Views;

/**
 * @author Stefano Ricci
 */
public class MainActivity extends BaseActivity {

    private SurveySpinnerAdapter surveyAdapter;
    private Spinner surveySpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        try {
            ServiceLocator.init(this);

            surveyAdapter = new SurveySpinnerAdapter(this);

            setContentView(R.layout.activity_main);

            TextView mainTitle = (TextView) findViewById(R.id.mainTitle);
            mainTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/caviar_dreams_normal.ttf"));

            TextView versionText = (TextView) findViewById(R.id.appVersion);
            versionText.setText(App.versionName(this));

            if (surveyAdapter.isSurveyListEmpty()) {
                Views.hide(findViewById(R.id.notEmptySurveyListFrame));
                Views.show(findViewById(R.id.emptySurveyListFrame));

                ((Button) findViewById(R.id.importDemoSurvey)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //empty survey list, show survey list activity
                        SurveyListActivity.startActivity(MainActivity.this);
                    }
                });

                ((Button) findViewById(R.id.importNewSurvey)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        handleImportNewSurvey();
                    }
                });
            } else {
                Views.hide(findViewById(R.id.emptySurveyListFrame));
                Views.show(findViewById(R.id.notEmptySurveyListFrame));

                initializeSurveySpinner();

                ((Button) findViewById(R.id.goToDataEntry)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int selectedSurveyPosition = surveySpinner.getSelectedItemPosition();
                        boolean surveySelected = surveyAdapter.isSurveyItem(selectedSurveyPosition);
                        handleGoToDataEntryButtonClick(surveySelected);
                    }
                });
            }
        } catch (WorkingDirNotWritable ignore) {
            DialogFragment newFragment = new SecondaryStorageNotFoundFragment();
            newFragment.show(getSupportFragmentManager(), "secondaryStorageNotFound");
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    private void initializeSurveySpinner() {
        surveySpinner = (Spinner) findViewById(R.id.surveySpinner);
        surveySpinner.setAdapter(surveyAdapter);
        surveySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (surveyAdapter.isImportSurveyItem(position)) {
                    handleImportNewSurvey();
                } else {
                    SurveyBaseAdapter.SurveyItem selectedSurveyItem = ((SurveyBaseAdapter.SurveyItem) surveyAdapter.getItem(position));
                    String selectedSurveyName = selectedSurveyItem.name;
                    handleSurveySelected(selectedSurveyName);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String currentSurveyName = SurveyImporter.selectedSurvey(this);
        surveySpinner.setSelection(surveyAdapter.getItemPosition(currentSurveyName));
    }

    private void handleSurveySelected(String selectedSurveyName) {
        String currentSurveyName = SurveyImporter.selectedSurvey(this);
        if (! selectedSurveyName.equals(currentSurveyName)) {
            SurveyImporter.selectSurvey(selectedSurveyName, this);
            SurveyNodeActivity.restartActivity(this);
        }
    }

    private void handleImportNewSurvey() {
        SurveyListActivity.showImportDialog(this);
    }

    private void handleGoToDataEntryButtonClick(boolean surveySelected) {
        if (surveySelected) {
            SurveyNodeActivity.restartActivity(this);
        } else {
            SurveyListActivity.startActivity(this);
        }
    }
}
