package org.openforis.collect.android.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.settings.SettingsActivity;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Keyboard;

/**
 * @author Stefano Ricci
 */
public abstract class BaseActivity extends AppCompatActivity {

    private void adjustFontScale(Configuration configuration) {
        org.openforis.collect.android.Settings.FontScale fontScale = org.openforis.collect.android.Settings.getFontScale();
        float systemScale = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE, 1f);
        configuration.fontScale = fontScale.getValue() * systemScale;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeInitializer.init(this);
        UILanguageInitializer.init(this);
        adjustFontScale(getResources().getConfiguration());
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
