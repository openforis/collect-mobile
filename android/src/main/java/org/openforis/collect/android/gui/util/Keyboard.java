package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {
    public static void show(View view, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showKeyboard = preferences.getBoolean("showKeyboard", true);
        if (showKeyboard) {
            view.requestFocus();
            inputMethodManager(context).showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void hide(Activity activity) {
        InputMethodManager inputMethodManager = inputMethodManager(activity);
        if (inputMethodManager != null) {
            if (activity == null)
                return;
            if (activity.getCurrentFocus() == null)
                return;
            if (activity.getCurrentFocus().getWindowToken() == null)
                return;
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private static InputMethodManager inputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
}
