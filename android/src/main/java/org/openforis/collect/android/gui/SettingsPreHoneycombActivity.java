package org.openforis.collect.android.gui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import org.openforis.collect.R;

/**
 * @author Daniel Wiell
 */
public class SettingsPreHoneycombActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);
        addPreferencesFromResource(R.xml.preferences_pre_honeycomb);
    }
}
