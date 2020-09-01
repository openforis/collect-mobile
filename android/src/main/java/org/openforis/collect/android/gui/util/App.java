package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

/**
 * @author Stefano Ricci
 */
public abstract class App {

    public static String versionName(Context context) {
        PackageInfo info = getPackageInfo(context);
        return info == null ? "-" : info.versionName;
    }

    public static int versionCode(Context context) {
        PackageInfo info = getPackageInfo(context);
        return info == null ? 0 : info.versionCode;
    }

    public static String versionFull(Context context) {
        return versionName(context) + " [" + versionCode(context) + "]";
    }

    @Nullable
    private static PackageInfo getPackageInfo(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo (context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
            info = null;
        }
        return info;
    }
}
