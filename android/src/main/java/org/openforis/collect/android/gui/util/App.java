package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author Stefano Ricci
 */
public abstract class App {

    public static String versionName(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo (context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
            info = null;
        }
        return info == null ? "-" : info.versionName;
    }
}
