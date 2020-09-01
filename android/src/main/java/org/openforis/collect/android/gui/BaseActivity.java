package org.openforis.collect.android.gui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        UILanguageInitializer.init(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Keyboard.hide(this);
    }

    protected void navigateToMainPage() {
        Activities.start(this, MainActivity.class);
    }

    public boolean navigateToSurveyList(MenuItem item) {
        navigateToSurveyList();
        return true;
    }

    protected void navigateToSurveyList() {
        Activities.start(this, SurveyListActivity.class);
    }

    public void navigateToSettings(MenuItem item) {
        Activities.start(this, SettingsActivity.class);
    }

    public void navigateToAboutPage(MenuItem item) {
        Activities.start(this, AboutActivity.class);
    }

    public void exit(MenuItem item) {
        exit();
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
