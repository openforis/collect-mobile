package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.openforis.collect.R;

/**
 * @author Daniel Wiell
 */
public class ThemeInitializer {
    public static void init(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean darkTheme = preferences.getBoolean("darkTheme", true);
        int theme = darkTheme ? R.style.AppTheme : R.style.AppTheme_Light;
        activity.setTheme(theme);
    }
}
