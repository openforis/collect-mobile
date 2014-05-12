package org.openforis.collect.android.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import org.openforis.collect.R;

/**
 * @author Daniel Wiell
 */
public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);
        addPreferencesFromResource(R.xml.preferences);
    }
}
