package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.openforis.collect.android.gui.WorkingDirNotWritable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkingDir {
    public static final String PREFERENCE_KEY = "workingDir";
    public static final String WORKING_DIR_NAME = "OpenForis Collect Mobile";

    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";

    public static File root(Context context) throws WorkingDirNotWritable {
        return workingDir(context);
    }

    public static File databases(Context context) throws WorkingDirNotWritable {
        return new File(root(context), "databases");
    }


    private static File workingDir(Context context) {
        File workingDir = readFromPreference(context);
        if (workingDir == null) {
            workingDir = guessSecondaryStorageWorkingDir();
            if (workingDir == null)
                workingDir = useExternalStorageDirectory();
            updatePreference(workingDir, context);
        }
        return workingDir;
    }

    private static File useExternalStorageDirectory() {
        return new File(Environment.getExternalStorageDirectory(), WORKING_DIR_NAME);
    }

    private static void updatePreference(File workingDir, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFERENCE_KEY, workingDir.getAbsolutePath());
        editor.commit();
    }

    private static File readFromPreference(Context context) {
        File workingDir = null;
        String path = PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_KEY, null);
        if (path != null)
            workingDir = new File(path);
        return workingDir;
    }

    private static File guessSecondaryStorageWorkingDir() {
        if (canGuessSecondaryStorage()) {
            File secondaryStorageLocation = WorkingDir.secondaryStorageLocation();
            File workingDir = new File(secondaryStorageLocation, WORKING_DIR_NAME);
            if (workingDir.exists() || workingDir.mkdirs())
                return workingDir;
        }
        return null;
    }


    private static File secondaryStorageLocation() {
        List<File> secondaryStorageLocations = secondaryStorageLocations();
        if (secondaryStorageLocations().isEmpty())
            return null;
        return secondaryStorageLocations.get(0);
    }

    public static boolean canGuessSecondaryStorage() {
        return !TextUtils.isEmpty(System.getenv(ENV_SECONDARY_STORAGE));
    }

    private static List<File> secondaryStorageLocations() {
        List<File> secondaryStorageLocations = new ArrayList<File>();
        final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
        if (!TextUtils.isEmpty(rawSecondaryStorage))
            for (String secondaryPath : rawSecondaryStorage.split(":")) {
                File path = new File(secondaryPath);
                if (path.canWrite())
                    secondaryStorageLocations.add(path);
            }
        return Collections.unmodifiableList(secondaryStorageLocations);
    }


}
