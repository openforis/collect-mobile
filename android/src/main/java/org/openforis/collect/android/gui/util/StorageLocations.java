package org.openforis.collect.android.gui.util;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageLocations {
    private static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";

    public static File secondaryStorageLocation() {
        List<File> secondaryStorageLocations = secondaryStorageLocations();
        if (secondaryStorageLocations().isEmpty())
            return null;
        return secondaryStorageLocations.get(0);
    }

    public static boolean hasSecondaryStorage() {
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
