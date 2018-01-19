package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by ricci on 17/01/18.
 */

public abstract class Activities {

    public static <A extends Activity> void start(Context context, Class<A> activityClass) {
        start(context, activityClass, 0, null);
    }

    public static <A extends Activity> void start(Context context, Class<A> activityClass, Bundle extras) {
        start(context, activityClass, 0, extras);
    }

    public static <A extends Activity> void start(Context context, Class<A> activityClass, int newActivityFlags, Bundle extras) {
        Keyboard.hide(context);
        Intent intent = new Intent(context, activityClass);
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | newActivityFlags;
        intent.setFlags(flags);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    public static <T extends Object> T getIntentExtra(Activity activity, String key) {
        Bundle extras = activity.getIntent().getExtras();
        if (extras == null) {
            return null;
        } else {
            return (T) extras.get(key);
        }
    }
}
