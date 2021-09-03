package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.openforis.collect.R;

/**
 * @author Daniel Wiell
 * @author Stefano Ricci
 */
public class ThemeInitializer {

    public static final String THEME_PREFERENCE_KEY = "theme";
    private static final String DARK_THEME_PREFERENCE_KEY = "darkTheme";

    public enum Theme {
        DARK(R.style.AppTheme),
        LIGHT(R.style.AppTheme_Light);

        int code;

        Theme(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static void init(Activity activity) {
        Theme theme = determineThemeFromPreferences(activity);
        activity.setTheme(theme.getCode());
    }

    public static Theme determineThemeFromPreferences(Activity activity) {
        SharedPreferences preferences = getPrefs(activity);
        String themeName = preferences.getString(THEME_PREFERENCE_KEY, null);
        if (themeName == null) {
            boolean darkTheme = preferences.getBoolean(DARK_THEME_PREFERENCE_KEY, true);
            return darkTheme ? Theme.DARK: Theme.LIGHT;
        } else {
            return Theme.valueOf(themeName.toUpperCase());
        }
    }

    private static SharedPreferences getPrefs(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity);
    }
}
