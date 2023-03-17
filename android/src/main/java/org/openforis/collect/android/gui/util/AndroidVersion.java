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
    private static boolean greaterEqualThan(int version) {
        return VERSION.SDK_INT >= version;
    }

    public static boolean greaterThan10() {
        return greaterThan(VERSION_CODES.GINGERBREAD_MR1);
    }

    public static boolean greaterThan11() {
        return greaterThan(VERSION_CODES.HONEYCOMB);
    }

    public static boolean greaterThan13() {
        return greaterThan(VERSION_CODES.HONEYCOMB_MR2);
    }

    public static boolean greaterThan14() {
        return greaterThan(VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    public static boolean greaterThan16() {
        return greaterThan(VERSION_CODES.JELLY_BEAN);
    }

    public static boolean greaterThan17() {
        return greaterThan(VERSION_CODES.JELLY_BEAN_MR1);
    }

    public static boolean greaterThan18() {
        return greaterThan(VERSION_CODES.JELLY_BEAN_MR2);
    }

    public static boolean greaterThan20() {
        return greaterThan(VERSION_CODES.KITKAT_WATCH);
    }

    public static boolean greaterEqualThan30() {
        return greaterEqualThan(VERSION_CODES.R);
    }

}
