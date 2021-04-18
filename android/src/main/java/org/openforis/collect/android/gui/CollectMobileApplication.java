package org.openforis.collect.android.gui;

import android.app.Activity;

import androidx.multidex.MultiDexApplication;

import com.mobilejazz.coltrane.provider.filesystem.FileSystemProvider;

/**
 * @author Stefano Ricci
 */
public class CollectMobileApplication extends MultiDexApplication {

    public static final String LOG_TAG = "CollectMobile";

    @Override
    public void onCreate() {
        super.onCreate();
        FileSystemProvider.register(getApplicationContext());
    }

    public static void exit(Activity context) {
        context.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
