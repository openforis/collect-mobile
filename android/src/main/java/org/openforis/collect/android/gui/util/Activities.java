package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import static android.content.Intent.*;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.WindowManager;

import java.io.File;

/**
 * @author Stefano Ricci
 */
public abstract class Activities {

    private static final String FILE_PROVIDER_AUTHORITY = "org.openforis.collect.fileprovider";

    public static <A extends Activity> void start(Context context, Class<A> activityClass) {
        start(context, activityClass, 0, null);
    }

    public static <A extends Activity> void start(Context context, Class<A> activityClass, Bundle extras) {
        start(context, activityClass, 0, extras);
    }

    public static <A extends Activity> void startNewClearTask(Context context, Class<A> activityClass) {
        startNewClearTask(context, activityClass, null);
    }

    public static <A extends Activity> void startNewClearTask(Context context, Class<A> activityClass, Bundle extras) {
        start(context, activityClass, FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK, extras);
    }

    public static <A extends Activity> void start(Context context, Class<A> activityClass, int newActivityFlags, Bundle extras) {
        Keyboard.hide(context);
        Intent intent = new Intent(context, activityClass);
        int flags = FLAG_ACTIVITY_NEW_TASK | newActivityFlags;
        intent.setFlags(flags);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    public static <T> T getIntentExtra(Activity activity, String key) {
        return getIntentExtra(activity, key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getIntentExtra(Activity activity, String key, T defaultValue) {
        Bundle extras = activity.getIntent().getExtras();
        if (extras == null) {
            return defaultValue;
        } else {
            Object val = extras.get(key);
            return val == null ? defaultValue : (T) val;
        }
    }

    public static void keepScreenOn(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static void clearKeepScreenOn(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static void shareFile(Context context, File file, String contentType, int messageKey, boolean viewOnly) {
        Intent intent = new Intent();

        Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);

        if (viewOnly) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, contentType);
        } else {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(contentType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, context.getText(messageKey)));
    }
}
