package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import org.openforis.collect.android.gui.WorkingDirNotWritable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AppDirs {
    public static final String PREFERENCE_KEY = "workingDir";
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";

    public static File root(Context context) throws WorkingDirNotWritable {
        File workingDir = readFromPreference(context);
        if (workingDir == null)
            workingDir = defaultWorkingDir(context);

        if (!workingDir.exists()) {
            if (!workingDir.mkdirs())
                throw new WorkingDirNotWritable();
            AndroidFiles.makeDiscoverable(workingDir, context);
        } else if (!workingDir.canWrite()) {
            throw new WorkingDirNotWritable();
        }
        return workingDir;
    }

    public static File surveyDatabasesDir(String surveyName, Context context) throws WorkingDirNotWritable {
        return new File(surveysDir(context), surveyName);
    }

    public static File surveysDir(Context context) throws WorkingDirNotWritable {
        return new File(root(context), "surveys");
    }

    private static File defaultWorkingDir(Context context) {
        File workingDir = sdCardDir(context);
        if (workingDir == null)
            workingDir = sdCardDirFromEnv();
        if (workingDir == null)
            workingDir = context.getExternalFilesDir(null);
        updatePreference(workingDir, context);
        AndroidFiles.makeDiscoverable(workingDir, context);
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
        return workingDir;
    }

    private static File sdCardDir(Context context) {
        File[] externalFilesDir = ContextCompat.getExternalFilesDirs(context, null);
        File emulatedStorageDir = Environment.isExternalStorageEmulated() ? context.getExternalFilesDir(null) : null;

        for (File dir : externalFilesDir)
            if (dir != null && (emulatedStorageDir == null || !dir.equals(emulatedStorageDir)))
                return dir;
        return null;
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
        File dir = first(secondaryStorageLocations);
        if (dir != null)
            dir = new File(dir, "collect-mobile");
        return dir;
    }

}
