package org.openforis.collect.android.gui;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;

/**
 * @author Stefano Ricci
 */
public class CollectMobileApplication extends MultiDexApplication {

    public static final String LOG_TAG = "CollectMobile";

    public static void exit(Activity context) {
        context.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
