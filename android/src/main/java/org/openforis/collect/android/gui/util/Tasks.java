package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.os.Handler;

import org.openforis.collect.R;

public class Tasks {

    public static void runSlowTask(Activity context, Runnable runnable) {
        runSlowTask(context, runnable, R.string.processing, R.string.please_wait);
    }

    public static void runSlowTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
        new SimpleSlowAsyncTask(context, runnable, progressDialogTitleResId, progressDialogMessageResId)
                .execute();
    }

    public static void runSlowTask(Activity context, Runnable runnable, SlowAsyncTask.ExceptionHandler exceptionHandler, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
        new SimpleSlowAsyncTask(context, runnable, exceptionHandler, progressDialogTitleResId, progressDialogMessageResId)
                .execute();
    }

    public static Handler runDelayed(final Runnable runnable, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(runnable, delay);
        return handler;
    }

    public static Handler runDelayedOnUiThread(final Activity context, final Runnable runnable, int delay) {
        return runDelayed(new Runnable() {
            public void run() {
                context.runOnUiThread(runnable);
            }
        }, delay);
    }
}
