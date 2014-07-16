package org.openforis.collect.android.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.StorageLocations;

/**
 * @author Daniel Wiell
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            setupUseSecondaryStoragePreference();
            setupCrewIdPreference();
        }

        private void setupCrewIdPreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Preference crewIdPreference = findPreference("crewId");
            crewIdPreference.setSummary(preferences.getString("crewId", ""));
            crewIdPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    crewIdPreference.setSummary(newValue.toString());
                    return true;
                }
            });
        }

        private void setupUseSecondaryStoragePreference() {
            final Preference useSecondaryStoragePreference = findPreference("useSecondaryStorage");
            useSecondaryStoragePreference.setDefaultValue(StorageLocations.hasSecondaryStorage());
            if (!StorageLocations.hasSecondaryStorage()) {
                SharedPreferences.Editor editor = useSecondaryStoragePreference.getEditor();
                editor.putBoolean("useSecondaryStorage", false);
                useSecondaryStoragePreference.setEnabled(false);
                editor.commit();
            }
        }
    }
}
