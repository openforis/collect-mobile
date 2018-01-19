package org.openforis.collect.android.gui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Keyboard;

/**
 * @author Stefano Ricci
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeInitializer.init(this);
        super.onCreate(savedInstanceState);
    }

    protected void navigateToSurveyList(MenuItem item) {
        navigateToSurveyList();
    }

    protected void navigateToSurveyList() {
        Activities.startActivity(this, SurveyListActivity.class);
    }

    protected void navigateToSettings(MenuItem item) {
        navigateToSettings();
    }

    protected void navigateToSettings() {
        Activities.startActivity(this, SettingsActivity.class);
    }

    protected void exit(MenuItem item) {
        exit();
    }

    protected void exit() {
        Dialogs.confirm(this, R.string.confirm_label, R.string.exit_confirm_message, new Runnable() {
            public void run() {
                BaseActivity.this.finish();
            }
        });
    }
}
