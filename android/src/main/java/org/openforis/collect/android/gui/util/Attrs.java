package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author Daniel Wiell
 */
public class Attrs {
    private final Context context;

    public Attrs(Context context) {
        this.context = context;
    }

    public int color(int attrId) {
        return context.getResources().getColor(resourceId(attrId));
    }

    @SuppressWarnings("ConstantConditions")
    public int resourceId(int attrId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.resourceId;
    }
}
