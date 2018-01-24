package org.openforis.collect.android.gui;

import android.support.multidex.MultiDexApplication;

/**
 * @author Stefano Ricci
 */
public class CollectMobileApplication extends MultiDexApplication {

    public static final String LOG_TAG = "CollectMobile";

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
    }
}
