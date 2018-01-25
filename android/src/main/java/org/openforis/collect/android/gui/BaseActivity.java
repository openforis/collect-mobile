package org.openforis.collect.android.gui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Dialogs;

/**
 * @author Stefano Ricci
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeInitializer.init(this);
        super.onCreate(savedInstanceState);
    }

    public boolean navigateToSurveyList(MenuItem item) {
        navigateToSurveyList();
        return true;
    }

    protected void navigateToSurveyList() {
        Activities.start(this, SurveyListActivity.class);
    }

    public boolean navigateToSettings(MenuItem item) {
        navigateToSettings();
        return true;
    }

    protected void navigateToSettings() {
        Activities.start(this, SettingsActivity.class);
    }

    public boolean exit(MenuItem item) {
        exit();
        return true;
    }

    protected void exit() {
        Dialogs.confirm(this, R.string.confirm_label, R.string.exit_confirm_message, new Runnable() {
            public void run() {
                BaseActivity.this.finish();
                Bundle bundle = new Bundle();
                bundle.putBoolean(MainActivity.EXIT_FLAG, true);
                Activities.startNewClearTask(BaseActivity.this, MainActivity.class, bundle);
            }
        });
    }
}
