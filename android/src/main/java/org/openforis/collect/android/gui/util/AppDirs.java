package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.openforis.collect.android.gui.WorkingDirNotWritable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class AppDirs {
    public static final String PREFERENCE_KEY = "workingDir";
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";

    public static File root(Context context) throws WorkingDirNotWritable {
        File workingDir = readFromPreference(context);
        if (workingDir == null) {
            workingDir = defaultWorkingDir(context);
            Log.i("CollectMobile", "Working dir - trying default: " + workingDir);
        }

        if (!workingDir.exists()) {
            if (!workingDir.mkdirs())
                throw new WorkingDirNotWritable(workingDir);
            AndroidFiles.makeDiscoverable(workingDir, context);
        } else if (!workingDir.canWrite())
            throw new WorkingDirNotWritable(workingDir);
        Log.i("CollectMobile", "Working dir: " + workingDir);
        return workingDir;
    }

    public static File surveyDatabasesDir(String surveyName, Context context) throws WorkingDirNotWritable {
        return new File(surveysDir(context), surveyName);
    }

    public static File surveysDir(Context context) throws WorkingDirNotWritable {
        return new File(root(context), "surveys");
    }

    public static File surveyImagesDir(String surveyName, Context context) throws WorkingDirNotWritable {
        return new File(surveyDatabasesDir(surveyName, context), "collect_upload");
    }

    public static File surveyGuideDir(Context context) {
        // it must be in internal or external Android app files dir, otherwise
        // the file cannot be viewed using an external app
        return context.getExternalFilesDir("survey_guide");
    }

    private static File defaultWorkingDir(Context context) {
        File workingDir = sdCardDir(context);
        if (workingDir == null)
            workingDir = sdCardDirFromEnv();
        if (workingDir == null)
            workingDir = externalFilesDir(context);
        if (workingDir == null)
            workingDir = filesDir(context);
        updatePreference(workingDir, context);
        AndroidFiles.makeDiscoverable(workingDir, context);
        return workingDir;
    }

    private static File filesDir(Context context) {
        File workingDir = context.getFilesDir();
        Log.d("CollectMobile", "Working dir - filesDir: " + workingDir);
        return workingDir;
    }

    private static File externalFilesDir(Context context) {
        File workingDir = context.getExternalFilesDir(null);
        Log.d("CollectMobile", "Working dir - getExternalFilesDir: " + workingDir);
        return workingDir;
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
        Log.d("CollectMobile", "Working dir - readFromPreference: " + workingDir);

        if (workingDir == null)
            return null;

        if (!workingDir.exists()) {
            if (!workingDir.mkdirs()) {
                Log.d("CollectMobile", "Working dir - readFromPreference - not writable: " + workingDir);
                return null;
            }
        }

        if (!workingDir.canWrite()) {
            Log.d("CollectMobile", "Working dir - readFromPreference - not writable: " + workingDir);
            return null;
        }

        return workingDir;
    }

    private static File sdCardDir(Context context) {
        File[] externalFilesDir = ContextCompat.getExternalFilesDirs(context, null);
        File emulatedStorageDir = Environment.isExternalStorageEmulated() ? context.getExternalFilesDir(null) : null;

        File workingDir = null;
        for (File dir : externalFilesDir)
            if (dir != null && (emulatedStorageDir == null || !dir.equals(emulatedStorageDir))) {
                workingDir = dir;
                break;
            }
        Log.d("CollectMobile", "Working dir - sdCardDir: " + workingDir);
        return workingDir;
    }

    private static <T> T first(Collection<T> list) {
        if (list == null)
            return null;
        Iterator<T> it = list.iterator();
        return it.hasNext() ? it.next() : null;
    }

    private static File sdCardDirFromEnv() {
        List<File> secondaryStorageLocations = new ArrayList<File>();
        final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
        if (!TextUtils.isEmpty(rawSecondaryStorage))
            for (String secondaryPath : rawSecondaryStorage.split(":")) {
                File path = new File(secondaryPath);
                if (path.canWrite())
                    secondaryStorageLocations.add(path);
            }
        Log.d("CollectMobile", "Working dir - secondaryStorageLocations: " + secondaryStorageLocations);
        File workingDir = first(secondaryStorageLocations);
        if (workingDir != null)
            workingDir = new File(workingDir, "collect-mobile");
        Log.d("CollectMobile", "Working dir - sdCardDirFromEnv: " + workingDir);
        return workingDir;
    }

}
