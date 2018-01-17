package org.openforis.collect.android.gui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyBaseAdapter;
import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.android.gui.util.Views;

/**
 * @author Stefano Ricci
 */
public class MainActivity extends AppCompatActivity {

    private SurveySpinnerAdapter surveyAdapter;
    private Spinner surveySpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        try {
            if (ServiceLocator.init(this)) {
                ThemeInitializer.init(this);
                super.onCreate(savedState);
            } else {
                super.onCreate(savedState); // TODO: Try to move this to beginning of method
            }
            surveyAdapter = new SurveySpinnerAdapter(this);

            final MainActivity context = this;

            setContentView(R.layout.activity_main);

            TextView mainTitle = (TextView) findViewById(R.id.mainTitle);
            mainTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/caviar_dreams_normal.ttf"));

            TextView versionText = (TextView) findViewById(R.id.appVersion);
            versionText.setText(App.versionName(this));

            if (surveyAdapter.getCount() == 1) {
                Views.hide(findViewById(R.id.notEmptySurveyListFrame));
                Views.show(findViewById(R.id.emptySurveyListFrame));

                ((Button) findViewById(R.id.importDemoSurvey)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //empty survey list, show survey list activity
                        SurveyListActivity.startActivity(context);
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
            super.onCreate(savedState);
            DialogFragment newFragment = new SecondaryStorageNotFoundFragment();
            newFragment.show(getSupportFragmentManager(), "secondaryStorageNotFound");
        }
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
