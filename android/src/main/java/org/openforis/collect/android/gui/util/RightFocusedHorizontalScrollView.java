package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * @author Daniel Wiell
 */
public class RightFocusedHorizontalScrollView extends HorizontalScrollView {
    public RightFocusedHorizontalScrollView(Context context) {
        super(context);
    }

    public RightFocusedHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RightFocusedHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        fullScroll(HorizontalScrollView.FOCUS_RIGHT);
    }
}
