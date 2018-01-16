package org.openforis.collect.android.gui;

import android.support.multidex.MultiDexApplication;

/**
 * @author Stefano Ricci
 */
public class CollectMobileApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
    }
}
