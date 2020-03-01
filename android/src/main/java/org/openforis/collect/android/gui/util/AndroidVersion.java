package org.openforis.collect.android.gui.util;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

/**
 * @author Daniel Wiell
 */
public class AndroidVersion {

    private static boolean greaterThan(int version) {
        return VERSION.SDK_INT > version;
    }

    public static boolean greaterThan10() {
        return greaterThan(VERSION_CODES.GINGERBREAD_MR1);
    }

    public static boolean greaterThan20() {
        return greaterThan(VERSION_CODES.KITKAT_WATCH);
    }
}
