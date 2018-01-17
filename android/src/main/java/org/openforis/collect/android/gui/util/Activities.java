package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.openforis.collect.android.gui.SurveyListActivity;

/**
 * Created by ricci on 17/01/18.
 */

public abstract class Activities {

    public static <A extends Activity> void startActivity(Activity context, Class<A> activityClass) {
        startActivity(context, activityClass, 0, null);
    }

    public static <A extends Activity> void startActivity(Activity context, Class<A> activityClass, Bundle extras) {
        startActivity(context, activityClass, 0, extras);
    }

    public static <A extends Activity> void startActivity(Activity context, Class<A> activityClass, int newActivityFlags, Bundle extras) {
        Keyboard.hide(context);
        Intent intent = new Intent(context, activityClass);
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK | newActivityFlags;
        intent.setFlags(flags);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    public static <O extends Object> O getExtra(Activity activity, String key) {
        Bundle extras = activity.getIntent().getExtras();
        if (extras == null) {
            return null;
        } else {
            return (O) extras.get(key);
        }
    }
}
