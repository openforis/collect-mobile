package org.openforis.collect.android.gui.util;

import android.view.View;
import android.widget.TextView;

import org.openforis.collect.android.util.StringUtils;

public abstract class Views {

    public static void setTextWithoutExceedingMaxWidht(TextView view, String text, int maxWidth) {
        view.setText(text);
        int maxLength = text.length();
        while (view.getWidth() > maxWidth) {
            maxLength--;
            view.setText(StringUtils.ellipsisMiddle(text, maxLength));
        }
    }


    public static void show(View rootView, int viewId) {
        toggleVisibility(rootView, viewId, true);
    }

    public static void show(View view) {
        toggleVisibility(view, true);
    }

    public static void hide(View rootView, int viewId) {
        toggleVisibility(rootView, viewId, false);
    }

    public static void hide(View view) {
        toggleVisibility(view, false);
    }

    public static void toggleVisibility(View rootView, int viewId, boolean visible) {
        toggleVisibility(rootView.findViewById(viewId), visible);
    }

    public static void toggleVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
