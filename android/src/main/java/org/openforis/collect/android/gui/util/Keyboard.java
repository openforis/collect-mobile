package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Keyboard {
    public static void show(View view, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showKeyboard = preferences.getBoolean("showKeyboard", true);
        if (showKeyboard) {
            view.requestFocus();
            inputMethodManager(context).showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    public static void hide(Context context) {
        if (context == null) {
            return;
        }
        InputMethodManager inputMethodManager = inputMethodManager(context);
        if (inputMethodManager == null)
            return;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            View currentFocus = activity.getCurrentFocus();
            if (!(currentFocus instanceof EditText) || currentFocus.getWindowToken() == null)
                inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            else
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private static InputMethodManager inputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

}
