package org.openforis.collect.android.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.codekidlabs.storagechooser.StorageChooser;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.R;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.SlowAsyncTask;
import org.openforis.collect.android.util.HttpConnectionHelper;
import org.openforis.collect.android.util.MessageSources;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.ResourceBundleMessageSource;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.Languages;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

/**
 * @author Daniel Wiell
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity {

    public static final String LANGUAGES_RESOURCE_BUNDLE_NAME = "org/openforis/collect/resourcebundles/languages";
    private static final MessageSource LANGUAGE_MESSAGE_SOURCE = new LanguagesResourceBundleMessageSource();
    private static final Map<String, String> LANGUAGES = createLanguagesData();

    public static final String PREFERENCE_WORKING_DIR_LOCATION = "workingDirLocation";
    public static final String CREW_ID = "crewId";
    public static final String COMPASS_ENABLED = "compassEnabled";
    private final static String SURVEY_PREFERRED_LANGUAGE_MODE = "survey_preferred_language_mode";
    private final static String SURVEY_PREFERRED_LANGUAGE_SPECIFIED = "survey_preferred_language_specified";
    private final static String LANGUAGE_UI_KEY = "language_ui";
    private final static String LANGUAGE_SURVEY_KEY = "language_survey";
    public static final String REMOTE_SYNC_ENABLED = "remoteSyncEnabled";
    public static final String REMOTE_COLLECT_ADDRESS = "remoteCollectAddress";
    public static final String REMOTE_COLLECT_USERNAME = "remoteCollectUsername";
    public static final String REMOTE_COLLECT_PASSWORD = "remoteCollectPassword";
    public static final String REMOTE_COLLECT_TEST = "remoteCollectTest";

    private enum WorkingDirLocation {
        EXTERNAL_SD_CARD("external_sd_card", R.string.settings_working_directory_external_sd_card),
        INTERNAL_EMULATED_SD_CARD("emulated_sd_card", R.string.settings_working_directory_emulated_sd_card),
        INTERNAL_MEMORY("internal_memory", R.string.settings_working_directory_internal_memory),
        CUSTOM_LOCATION("custom", R.string.settings_working_directory_custom);

        private final String key;
        private final int summaryKey;

        WorkingDirLocation(String key, int summaryKey) {
            this.key = key;
            this.summaryKey = summaryKey;
        }

        static String[] keys() {
            WorkingDirLocation[] values = values();
            List<String> result = new ArrayList<String>(values.length);
            for (WorkingDirLocation location : values) {
                result.add(location.key);
            }
            return result.toArray(new String[values.length]);
        }

        static WorkingDirLocation getByKey(String key) {
            for (WorkingDirLocation location : values()) {
                if (location.key.equals(key)) return location;
            }
            return null;
        }

        public static WorkingDirLocation getCurrent(Context context) {
            if (AppDirs.isRootSdCard(context)) {
                return WorkingDirLocation.EXTERNAL_SD_CARD;
            } else if (AppDirs.isRootInternalEmulatedSdCard(context)) {
                return WorkingDirLocation.INTERNAL_EMULATED_SD_CARD;
            } else if (AppDirs.isRootInternalFiles(context)) {
                return WorkingDirLocation.INTERNAL_MEMORY;
            } else {
                return WorkingDirLocation.CUSTOM_LOCATION;
            }
        }

        File getFile(Context context) {
            switch (this) {
                case EXTERNAL_SD_CARD:
                    return AppDirs.sdCardDir(context);
                case INTERNAL_EMULATED_SD_CARD:
                    return AppDirs.externalFilesDir(context);
                case INTERNAL_MEMORY:
                    return AppDirs.filesDir(context);
                case CUSTOM_LOCATION:
                default:
                    return null;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeInitializer.init(this);
        // Display the fragment as the main content.
        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    public static void init(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Settings.setCrew(preferences.getString(CREW_ID, ""));
        Settings.setCompassEnabled(preferences.getBoolean(COMPASS_ENABLED, true));
        Settings.setPreferredLanguageMode(Settings.PreferredLanguageMode.valueOf(preferences.getString(SURVEY_PREFERRED_LANGUAGE_MODE,
                Settings.PreferredLanguageMode.SYSTEM_DEFAULT.name())));
        Settings.setPreferredLanguage(preferences.getString(SURVEY_PREFERRED_LANGUAGE_SPECIFIED, Locale.getDefault().getLanguage()));
        Settings.setRemoteSyncEnabled(preferences.getBoolean(REMOTE_SYNC_ENABLED, false));
        Settings.setRemoteCollectAddress(preferences.getString(REMOTE_COLLECT_ADDRESS, ""));
        Settings.setRemoteCollectUsername(preferences.getString(REMOTE_COLLECT_USERNAME, ""));
        Settings.setRemoteCollectPassword(preferences.getString(REMOTE_COLLECT_PASSWORD, ""));
    }

    @Override
    public void onBackPressed() {
        SurveyNodeActivity.restartActivity(this);
        super.onBackPressed();
    }

    protected void onWorkingDirectorySelect(final String path) {
        final Activity activity = this;

        String oldPath = AppDirs.rootAbsolutePath(activity);
        if (oldPath.equals(path)) {
            // do nothing
            return;
        }
        // check that selected path is writable
        if (!new File(path).canWrite()) {
            Dialogs.alert(activity, R.string.warning, R.string.settings_error_working_directory);
            return;
        }

        // confirm working directory change
        Dialogs.confirm(activity, R.string.confirm_label, getString(R.string.settings_working_directory_confirm_change, oldPath, path), new Runnable() {
            public void run() {
                // save working directory to preferences
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(AppDirs.PREFERENCE_KEY, path);

                editor.apply();

                // reset service locator and restart main activity
                ServiceLocator.reset(activity);
                Activities.startNewClearTask(activity, MainActivity.class);
                activity.finish();
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragment {

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setupWorkingDirPreference();
            setupCrewIdPreference();
            setupCompassEnabledPreference();
            setupThemePreference();
            setupLanguagePreference();
            setupRemoteSyncEnabledPreference();
            setupRemoteCollectAddressPreference();
            setupRemoteCollectUsernamePreference();
            setupRemoteCollectPasswordPreference();
            setupRemoteCollectConnectionTestPreference();
        }

        private void updateWorkingDirSummary() {
            Activity activity = getActivity();

            WorkingDirLocation location = WorkingDirLocation.getCurrent(activity);
            File currentWorkingDir = AppDirs.root(activity);

            Preference preference = findPreference(PREFERENCE_WORKING_DIR_LOCATION);
            preference.setSummary(getString(location.summaryKey,
                    AndroidFiles.availableSpaceMB(currentWorkingDir),
                    currentWorkingDir.getAbsolutePath()
                ));
        }

        private void setupWorkingDirPreference() {
            final SettingsActivity activity = (SettingsActivity) getActivity();

            ListPreference preference = (ListPreference) findPreference(PREFERENCE_WORKING_DIR_LOCATION);

            WorkingDirLocation[] locations = WorkingDirLocation.values();
            List<String> entries = new ArrayList<String>(locations.length);
            for (WorkingDirLocation location : locations) {
                File file = location.getFile(activity);
                String path = file == null ? "" : file.getAbsolutePath();
                long availableSpace = file == null ? 0 : AndroidFiles.availableSpaceMB(file);
                entries.add(activity.getString(location.summaryKey, availableSpace, path));
            }
            preference.setEntries(entries.toArray(new String[0]));
            preference.setEntryValues(WorkingDirLocation.keys());

            updateWorkingDirSummary();

            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    WorkingDirLocation location = WorkingDirLocation.getByKey((String) newValue);
                    File file = location.getFile(activity);
                    if (file != null) {
                        activity.onWorkingDirectorySelect(file.getAbsolutePath());
                    } else {
                        final StorageChooser chooser = new StorageChooser.Builder()
                                .withActivity(activity)
                                .withFragmentManager(getFragmentManager())
                                .withMemoryBar(true)
                                .allowCustomPath(true)
                                .allowAddFolder(true)
                                .setType(StorageChooser.DIRECTORY_CHOOSER)
                                .withPredefinedPath(AppDirs.rootAbsolutePath(activity))
                                .build();

                        // handle path that the user has chosen
                        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                            public void onSelect(final String path) {
                                activity.onWorkingDirectorySelect(path);
                            }
                        });
                        chooser.show();
                    }
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

        private void setupThemePreference() {
            Preference preference = findPreference(ThemeInitializer.THEME_PREFERENCE_KEY);
            ThemeInitializer.Theme theme = ThemeInitializer.determineThemeFromPreferences(getActivity());
            preference.setSummary(getThemeSummary(theme.name()));
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(getThemeSummary((String) newValue));
                    return true;
                }
            });
        }

        private void setupLanguagePreference() {
            final Preference preferredLanguageModePreference = findPreference(SURVEY_PREFERRED_LANGUAGE_MODE);
            final ListPreference preferredLanguagePreference = (ListPreference) findPreference(SURVEY_PREFERRED_LANGUAGE_SPECIFIED);

            preferredLanguagePreference.setEntries(LANGUAGES.values().toArray(new String[0]));
            preferredLanguagePreference.setEntryValues(LANGUAGES.keySet().toArray(new String[0]));

            updateLanguagePreferenceUI();

            preferredLanguageModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.PreferredLanguageMode newPreferredLanguageMode = Settings.PreferredLanguageMode.valueOf((String) newValue);
                    Settings.setPreferredLanguageMode(newPreferredLanguageMode);

                    String preferredLanguageCode = newPreferredLanguageMode == Settings.PreferredLanguageMode.SPECIFIED
                            ? Locale.getDefault().getLanguage()
                            : null;

                    Settings.setPreferredLanguage(preferredLanguageCode);
                    preferredLanguagePreference.setValue(preferredLanguageCode);

                    handleLanguageChanged(getActivity());
                    return true;
                }
            });

            preferredLanguagePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.setPreferredLanguage((String) newValue);
                    handleLanguageChanged(getActivity());
                    return true;
                }
            });
        }

        private String getPreferredLanguageModeSummary(Settings.PreferredLanguageMode mode) {
            switch (mode) {
                case SYSTEM_DEFAULT:
                    return getString(R.string.settings_preferred_language_mode_system_default);
                case SURVEY_DEFAULT:
                    return getString(R.string.settings_preferred_language_mode_survey_default);
                default:
                    return getString(R.string.settings_preferred_language_mode_specified);
            }
        }

        private String determinePreferredLanguageCode() {
            CollectSurvey selectedSurvey = getSelectedSurvey();
            switch (Settings.getPreferredLanguageMode()) {
                case SYSTEM_DEFAULT:
                    Locale systemLocale = Resources.getSystem().getConfiguration().locale;
                    return systemLocale.getLanguage();
                case SURVEY_DEFAULT:
                    return selectedSurvey == null
                            ? null
                            : selectedSurvey.getDefaultLanguage();
                case SPECIFIED:
                    return Settings.getPreferredLanguage();
                default:
                    return null;
            }
        }

        private String getThemeSummary(String themeName) {
            return getString(themeName.equalsIgnoreCase(ThemeInitializer.Theme.DARK.name())
                    ? R.string.settings_theme_dark_summary
                    : R.string.settings_theme_light_summary
            );
        }

        private void setupRemoteSyncEnabledPreference() {
            Preference preference = findPreference(REMOTE_SYNC_ENABLED);
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.setRemoteSyncEnabled((Boolean) newValue);
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
            preference.setSummary(StringUtils.isNotBlank(preferences.getString(REMOTE_COLLECT_PASSWORD, "")) ? "*********" : "");
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringVal = newValue.toString();
                    preference.setSummary(StringUtils.isNotBlank(stringVal) ? "*********" : "");
                    Settings.setRemoteCollectPassword(stringVal);
                    return true;
                }
            });
        }

        private void setupRemoteCollectConnectionTestPreference() {
            Preference preference = findPreference(REMOTE_COLLECT_TEST);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    if (Permissions.checkInternetPermissionOrRequestIt(getActivity())) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String rootAddress = preferences.getString(SettingsActivity.REMOTE_COLLECT_ADDRESS, "");
                        String username = preferences.getString(SettingsActivity.REMOTE_COLLECT_USERNAME, "");
                        String password = preferences.getString(SettingsActivity.REMOTE_COLLECT_PASSWORD, "");
                        String address = rootAddress + (rootAddress.endsWith("/") ? "" : "/") + "api/info";
                        new RemoteConnectionTestTask(getActivity(), address, username, password)
                                .execute();
                    }
                    return false;
                }
            });
        }

        private void handleLanguageChanged(Context context) {
            UILanguageInitializer.init(context);
            ServiceLocator.resetModelManager(context);

            updateLanguagePreferenceUI();
        }

        private void updateLanguagePreferenceUI() {
            // preferred language mode
            final Preference preferredLanguageModePreference = findPreference(SURVEY_PREFERRED_LANGUAGE_MODE);

            Settings.PreferredLanguageMode preferredLanguageMode = Settings.getPreferredLanguageMode();
            preferredLanguageModePreference.setSummary(getPreferredLanguageModeSummary(preferredLanguageMode));

            // preferred language specified
            final ListPreference preferredLanguagePreference = (ListPreference) findPreference(SURVEY_PREFERRED_LANGUAGE_SPECIFIED);

            preferredLanguagePreference.setEnabled(Settings.PreferredLanguageMode.SPECIFIED == preferredLanguageMode);
            preferredLanguagePreference.setSummary(getLanguageLabel(Settings.getPreferredLanguage()));

            String preferredLangCode = determinePreferredLanguageCode();

            // language UI
            final Preference languageUiPreference = findPreference(LANGUAGE_UI_KEY);

            languageUiPreference.setSummary(preferredLangCode == null ? null : Settings.UILanguage.isSupported(preferredLangCode)
                    ? getLanguageLabel(preferredLangCode)
                    : getLanguageLabel(Settings.UILanguage.getDefault().getCode())
            );

            // language Survey
            final Preference languageSurveyPreference = findPreference(LANGUAGE_SURVEY_KEY);
            CollectSurvey selectedSurvey = getSelectedSurvey();
            if (selectedSurvey == null) {
                languageSurveyPreference.setEnabled(false);
                languageSurveyPreference.setSummary(null);
            } else {
                languageSurveyPreference.setEnabled(true);
                String summary;
                if (preferredLangCode == null) {
                    summary = null;
                } else {
                    List<String> availableLanguages = selectedSurvey.getLanguages();
                    String surveyLanguageUsed = availableLanguages.contains(preferredLangCode)
                            ? preferredLangCode
                            : selectedSurvey.getDefaultLanguage();
                    summary = getLanguageLabel(surveyLanguageUsed);
                }
                languageSurveyPreference.setSummary(summary);
            }
        }

        @Nullable
        private CollectSurvey getSelectedSurvey() {
            return ServiceLocator.surveyService() == null
                    ? null
                    : ServiceLocator.surveyService().getSelectedSurvey();
        }
    }

    private static Map<String, String> createLanguagesData() {
        List<String> langCodes = Languages.getCodes(Languages.Standard.ISO_639_1);
        Map<String, String> unsortedLanguages = new HashMap<String, String>(langCodes.size());
        for (String langCode : langCodes) {
            unsortedLanguages.put(langCode, getLanguageLabel(langCode));
        }
        List<Map.Entry<String, String>> entries = new LinkedList<Map.Entry<String, String>>(unsortedLanguages.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static String getLanguageLabel(String langCode) {
        if (langCode == null)
            return null;
        String label = MessageSources.getMessage(LANGUAGE_MESSAGE_SOURCE, langCode);
        return String.format("%s (%s)", label, langCode);
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
            if (info != null) {
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

    private static class LanguagesResourceBundleMessageSource extends ResourceBundleMessageSource {

        LanguagesResourceBundleMessageSource() {
            super(Collections.singletonList(LANGUAGES_RESOURCE_BUNDLE_NAME));
        }

        protected PropertyResourceBundle findBundle(Locale locale, String baseName) {
            return (PropertyResourceBundle) PropertyResourceBundle.getBundle(baseName, locale);
        }
    }
}
