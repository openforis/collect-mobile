package org.openforis.collect.android.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.R;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.SlowAsyncTask;
import org.openforis.collect.android.util.HttpConnectionHelper;
import org.openforis.commons.versioning.Version;

import java.io.File;
import java.io.FileNotFoundException;

import static org.openforis.collect.android.gui.util.AppDirs.PREFERENCE_KEY;

/**
 * @author Daniel Wiell
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity implements DirectoryChooserFragment.OnFragmentInteractionListener {
    public static final String CREW_ID = "crewId";
    public static final String COMPASS_ENABLED = "compassEnabled";
    public static final String REMOTE_SYNC_ENABLED = "remoteSyncEnabled";
    public static final String REMOTE_COLLECT_ADDRESS = "remoteCollectAddress";
    public static final String REMOTE_COLLECT_USERNAME = "remoteCollectUsername";
    public static final String REMOTE_COLLECT_PASSWORD = "remoteCollectPassword";
    public static final String REMOTE_COLLECT_TEST = "remoteCollectTest";

    private DirectoryChooserFragment directoryChooserDialog;
    private SettingsFragment settingsFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);
        File workingDir = AppDirs.root(this);
        directoryChooserDialog = DirectoryChooserFragment.newInstance(workingDir.getName(), workingDir.getParent());


        // Display the fragment as the main content.
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    public static void init(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Settings.setCrew(preferences.getString(CREW_ID, ""));
        Settings.setCompassEnabled(preferences.getBoolean(COMPASS_ENABLED, true));
        Settings.setRemoteSyncEnabled(preferences.getBoolean(REMOTE_SYNC_ENABLED, false));
        Settings.setRemoteCollectAddress(preferences.getString(REMOTE_COLLECT_ADDRESS, ""));
        Settings.setRemoteCollectUsername(preferences.getString(REMOTE_COLLECT_USERNAME, ""));
        Settings.setRemoteCollectPassword(preferences.getString(REMOTE_COLLECT_PASSWORD, ""));
    }

    public void onSelectDirectory(@NonNull String workingDir) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_KEY, workingDir);
        editor.commit();
        Preference workingDirPreference = settingsFragment.findPreference(PREFERENCE_KEY);
        workingDirPreference.setSummary(workingDir);
        directoryChooserDialog.dismiss();
        ServiceLocator.reset(this);
        Activities.startNewClearTask(this, MainActivity.class);
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
            setupCompassEnabledPreference();
            setupRemoteSyncEnabledPreference();
            setupRemoteCollectAddressPreference();
            setupRemoteCollectUsernamePreference();
            setupRemoteCollectPasswordPreference();
            setupRemoteCollectConnectionTestPreference();
        }

        private void setupStorageLocationPreference() {
            Preference workingDirPreference = findPreference(PREFERENCE_KEY);
            workingDirPreference.setSummary(AppDirs.root(getActivity()).getAbsolutePath());
            workingDirPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).directoryChooserDialog.show(getFragmentManager(), null);
                    return true;
                }
            });
        }

        private void setupCrewIdPreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference preference = findPreference(CREW_ID);
            preference.setSummary(preferences.getString(CREW_ID, ""));
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String crew = newValue.toString();
                    preference.setSummary(crew);
                    Settings.setCrew(crew);
                    return true;
                }
            });
        }

        private void setupCompassEnabledPreference() {
            Preference preference = findPreference(COMPASS_ENABLED);
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.setCompassEnabled((Boolean) newValue);
                    return true;
                }
            });
        }

        private void setupRemoteSyncEnabledPreference() {
            Preference preference = findPreference(REMOTE_SYNC_ENABLED);
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enabled = (Boolean) newValue;
                    Settings.setRemoteSyncEnabled(enabled);

                    findPreference(REMOTE_COLLECT_ADDRESS).setEnabled(enabled);
                    findPreference(REMOTE_COLLECT_USERNAME).setEnabled(enabled);
                    findPreference(REMOTE_COLLECT_PASSWORD).setEnabled(enabled);
                    return true;
                }
            });
        }

        private void setupRemoteCollectAddressPreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference preference = findPreference(REMOTE_COLLECT_ADDRESS);
            preference.setSummary(preferences.getString(REMOTE_COLLECT_ADDRESS, ""));
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringVal = newValue.toString();
                    preference.setSummary(stringVal);
                    Settings.setRemoteCollectAddress(stringVal);
                    return true;
                }
            });
        }

        private void setupRemoteCollectUsernamePreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference preference = findPreference(REMOTE_COLLECT_USERNAME);
            preference.setSummary(preferences.getString(REMOTE_COLLECT_USERNAME, ""));
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringVal = newValue.toString();
                    preference.setSummary(stringVal);
                    Settings.setRemoteCollectUsername(stringVal);
                    return true;
                }
            });
        }

        private void setupRemoteCollectPasswordPreference() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference preference = findPreference(REMOTE_COLLECT_PASSWORD);
            preference.setSummary(StringUtils.isNotBlank(preferences.getString(REMOTE_COLLECT_PASSWORD, "")) ? "*********": "");
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringVal = newValue.toString();
                    preference.setSummary(StringUtils.isNotBlank(stringVal) ? "*********": "");
                    Settings.setRemoteCollectPassword(stringVal);
                    return true;
                }
            });
        }

        private void setupRemoteCollectConnectionTestPreference() {
            Preference preference = findPreference(REMOTE_COLLECT_TEST);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String rootAddress = preferences.getString(SettingsActivity.REMOTE_COLLECT_ADDRESS, "");
                    String username = preferences.getString(SettingsActivity.REMOTE_COLLECT_USERNAME, "");
                    String password = preferences.getString(SettingsActivity.REMOTE_COLLECT_PASSWORD, "");
                    String address = rootAddress + (rootAddress.endsWith("/") ? "" : "/") + "api/info";
                    new RemoteConnectionTestTask(getActivity(), address, username, password).execute();
                    return false;
                }
            });
        }
    }

    private static class RemoteConnectionTestTask extends SlowAsyncTask<Void, Void, JsonObject> {

        private final String address;
        private final String username;
        private final String password;

        RemoteConnectionTestTask(Activity context, String address, String username, String password) {
            super(context);
            this.address = address;
            this.username = username;
            this.password = password;
        }

        protected JsonObject runTask() throws Exception {
            HttpConnectionHelper connectionHelper = new HttpConnectionHelper(address, username, password);
            return connectionHelper.getJson();
        }

        @Override
        protected void onPostExecute(JsonObject info) {
            super.onPostExecute(info);
            if (info == null) {
                //handled in handleException
            } else {
                String remoteCollectVersionStr = info.get("version").getAsString();
                Version remoteCollectVersion = new Version(remoteCollectVersionStr);

                if (Collect.VERSION.compareTo(remoteCollectVersion, Version.Significance.MINOR) > 0) {
                    String message = context.getString(R.string.settings_remote_sync_test_failed_message_newer_version,
                            remoteCollectVersion.toString(), Collect.VERSION.toString());
                    Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_failed_title), message);
                } else {
                    Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_successful_title),
                            context.getString(R.string.settings_remote_sync_test_successful_message));
                }
            }
        }

        @Override
        protected void handleException(Exception e) {
            super.handleException(e);
            String message;
            if (e instanceof FileNotFoundException) {
                message = context.getString(R.string.settings_remote_sync_test_failed_message_wrong_address);
            } else {
                message = e.getMessage();
            }
            Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_failed_title), message);
        }
    }
}
