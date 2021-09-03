package org.openforis.collect.android.gui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.codekidlabs.storagechooser.StorageChooser;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.MainActivity;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.gui.util.Dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class WorkingDirectoryPreferenceInitializer {

    private static final String PREFERENCE_WORKING_DIR_LOCATION = "workingDirLocation";

    private enum WorkingDirLocation {
        EXTERNAL_SD_CARD("external_sd_card", R.string.settings_working_directory_external_sd_card),
        INTERNAL_EMULATED_SD_CARD("emulated_sd_card", R.string.settings_working_directory_emulated_sd_card),
        INTERNAL_MEMORY("internal_memory", R.string.settings_working_directory_internal_memory),
        CUSTOM("custom", R.string.settings_working_directory_custom, R.string.settings_working_directory_custom_entry);

        private final String key;
        private final int summaryKey;
        private final int entryLabelKey;

        WorkingDirLocation(String key, int summaryKey) {
            this(key, summaryKey, summaryKey);
        }

        WorkingDirLocation(String key, int summaryKey, int entryLabelKey) {
            this.key = key;
            this.summaryKey = summaryKey;
            this.entryLabelKey = entryLabelKey;
        }

        @NonNull
        static WorkingDirLocation getByKey(String key) {
            for (WorkingDirLocation location : values()) {
                if (location.key.equals(key)) return location;
            }
            return WorkingDirLocation.CUSTOM;
        }

        public static WorkingDirLocation getCurrent(Context context) {
            if (AppDirs.isRootSdCard(context)) {
                return WorkingDirLocation.EXTERNAL_SD_CARD;
            } else if (AppDirs.isRootInternalEmulatedSdCard(context)) {
                return WorkingDirLocation.INTERNAL_EMULATED_SD_CARD;
            } else if (AppDirs.isRootInternalFiles(context)) {
                return WorkingDirLocation.INTERNAL_MEMORY;
            } else {
                return WorkingDirLocation.CUSTOM;
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
                case CUSTOM:
                default:
                    return null;
            }
        }

        String getLocationLabel(Context context, File file, boolean useEntryLabel) {
            String path = file == null ? "" : file.getAbsolutePath();
            long availableSpace = file == null ? 0 : AndroidFiles.availableSpaceMB(file);
            if (availableSpace == Long.MAX_VALUE) availableSpace = 0;

            return context.getString(useEntryLabel ? entryLabelKey : summaryKey, availableSpace, path);
        }

        String getSummaryLabel(Context context) {
            File file = AppDirs.root(context);
            return getLocationLabel(context, file, false);
        }

        String getEntryLabel(Context context) {
            File file = getFile(context);
            return getLocationLabel(context, file, true);
        }
    }

    private final PreferenceFragment preferenceFragment;

    public WorkingDirectoryPreferenceInitializer(PreferenceFragment preferenceFragment) {
        this.preferenceFragment = preferenceFragment;
    }

    protected void onWorkingDirectorySelect(final String path) {
        final Activity activity = preferenceFragment.getActivity();

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
        Dialogs.confirm(activity, R.string.confirm_label, activity.getString(R.string.settings_working_directory_confirm_change, oldPath, path), new Runnable() {
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

    public void setupPreference() {
        final Activity context = preferenceFragment.getActivity();

        ListPreference preference = (ListPreference) preferenceFragment.findPreference(PREFERENCE_WORKING_DIR_LOCATION);

        WorkingDirLocation[] locations = WorkingDirLocation.values();
        List<String> entries = new ArrayList<String>(locations.length);
        List<String> entryValues = new ArrayList<String>(locations.length);
        for (WorkingDirLocation location : locations) {
            File file = location.getFile(context);
            if (location == WorkingDirLocation.CUSTOM || file != null && (file.exists() || file.mkdirs()) && file.canWrite()) {
                entryValues.add(location.key);
                entries.add(location.getEntryLabel(context));
            }
        }
        preference.setEntryValues(entryValues.toArray(new String[0]));
        preference.setEntries(entries.toArray(new String[0]));

        WorkingDirLocation location = WorkingDirLocation.getCurrent(context);
        preference.setSummary(location.getSummaryLabel(context));

        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                WorkingDirLocation location = WorkingDirLocation.getByKey((String) newValue);

                File file = location.getFile(context);
                if (file != null) {
                    onWorkingDirectorySelect(file.getAbsolutePath());
                } else {
                    final StorageChooser chooser = new StorageChooser.Builder()
                            .withActivity(context)
                            .withFragmentManager(preferenceFragment.getFragmentManager())
                            .withMemoryBar(true)
                            .allowCustomPath(true)
                            .allowAddFolder(true)
                            .setType(StorageChooser.DIRECTORY_CHOOSER)
                            .withPredefinedPath(AppDirs.rootAbsolutePath(context))
                            .build();

                    // handle path that the user has chosen
                    chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                        public void onSelect(final String path) {
                            onWorkingDirectorySelect(path);
                        }
                    });
                    chooser.show();
                }
                return true;
            }
        });
    }
}
