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

    public static void toggleVisibility(View rootView, int viewId, boolean visible) {
        toggleVisibility(rootView.findViewById(viewId), visible);
    }

    public static void toggleVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
