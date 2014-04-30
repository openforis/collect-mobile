package org.openforis.collect.android.gui.util;

import android.os.Build;

/**
 * @author Daniel Wiell
 */
public class AndroidVersion {
    public static boolean greaterThan10() {
        return android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static boolean greaterThan17() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
}
