package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openforis.collect.android.util.StringUtils;

import java.util.Stack;

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
        hide(rootView, viewId, true);
    }

    public static void hide(View rootView, int viewId, boolean gone) {
        hide(rootView.findViewById(viewId), gone);
    }

    public static void hide(View view) {
        hide(view, true);
    }

    public static void hide(View view, boolean gone) {
        view.setVisibility(gone ? View.GONE : View.INVISIBLE);
    }

    public static void toggleVisibility(View rootView, int viewId, boolean visible) {
        toggleVisibility(rootView.findViewById(viewId), visible);
    }

    public static void toggleVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static int px(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static <T extends View> T findDescendant(View parent, int viewId) {
        Stack<View> stack = new Stack<View>();
        stack.push(parent);
        while (!stack.isEmpty()) {
            View currentView = stack.pop();
            View view = currentView.findViewById(viewId);
            if (view != null) {
                return (T) view;
            }
            if (currentView instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) currentView;
                for (int i = 0; i < group.getChildCount(); i++) {
                    stack.push(group.getChildAt(i));
                }
            }
        }
        return null;
    }

    public static boolean hasChild(ViewGroup viewGroup, View child) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (viewGroup.getChildAt(i) == child) return true;
        }
        return false;
    }

    public static void addChild(ViewGroup viewGroup, View child) {
        if (!hasChild(viewGroup, child)) {
            viewGroup.addView(child);
        }
    }
}
