package org.openforis.collect.android.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.WorkingDir;

import static org.openforis.collect.android.gui.util.WorkingDir.PREFERENCE_KEY;

/**
 * @author Daniel Wiell
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity implements DirectoryChooserFragment.OnFragmentInteractionListener {
    private static final String CREW_ID = "crewId";
    private DirectoryChooserFragment directoryChooserDialog;
    private SettingsFragment settingsFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);
        directoryChooserDialog = DirectoryChooserFragment.newInstance(WorkingDir.WORKING_DIR_NAME, WorkingDir.root(this).getAbsolutePath());


        // Display the fragment as the main content.
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    public void onSelectDirectory(@NonNull String workingDir) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_KEY, workingDir);
        editor.commit();
        Preference workingDirPreference = settingsFragment.findPreference(PREFERENCE_KEY);
        workingDirPreference.setSummary(workingDir);
        directoryChooserDialog.dismiss();
        ServiceLocator.reset();
        SurveyNodeActivity.restartActivity(this);
        this.finish();
    }

    public void onCancelChooser() {
        directoryChooserDialog.dismiss();
    }

    public static class SettingsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setupStorageLocationPreference();
            setupCrewIdPreference();
        }

        private void setupStorageLocationPreference() {
            Preference workingDirPreference = findPreference(PREFERENCE_KEY);
            workingDirPreference.setSummary(WorkingDir.root(getActivity()).getAbsolutePath());
            workingDirPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).directoryChooserDialog.show(getFragmentManager(), null);
                    return true;
                }
            });
        }

        private void setupCrewIdPreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Preference crewIdPreference = findPreference(CREW_ID);
            crewIdPreference.setSummary(preferences.getString(CREW_ID, ""));
            crewIdPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    crewIdPreference.setSummary(newValue.toString());
                    return true;
                }
            });
        }
    }
}
