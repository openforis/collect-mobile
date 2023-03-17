package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public abstract class AppearancePreferencesApplier {

    public static void applyPreferences(Activity context) {
        adjustFontScale(context);
        adjustOrientation(context);
    }

    private static void adjustFontScale(Context context) {
        org.openforis.collect.android.Settings.FontScale fontScale = org.openforis.collect.android.Settings.getFontScale();
        float systemScale = Settings.System.getFloat(context.getContentResolver(), Settings.System.FONT_SCALE, 1f);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = fontScale.getValue() * systemScale;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }

    private static void adjustOrientation(Activity context) {
        int orientation = org.openforis.collect.android.Settings.isLockScreenToPortraitMode() ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        context.setRequestedOrientation(orientation);
    }

}
