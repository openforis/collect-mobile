package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import org.openforis.collect.R;

public class Tasks {

    public static void runSlowTask(Activity context, Runnable runnable) {
        runSlowTask(context, runnable, R.string.processing, R.string.please_wait);
    }

    public static void runSlowTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
        runSlowTask(context, runnable, new DefaultExceptionHandler(context), progressDialogTitleResId, progressDialogMessageResId);
    }

    public static void runSlowTask(Activity context, final Runnable runnable, SlowAsyncTask.ExceptionHandler exceptionHandler, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        final Handler handler = new Handler(Looper.getMainLooper());
//        executor.execute(new Runnable() {
//                             @Override
//                             public void run() {
//                                 handler.post(new Runnable() {
//                                     @Override
//                                     public void run() {
//
//                                         runnable.run();
//                                     }
//                                 });
//                             }
//                         });
//

        new SimpleSlowAsyncTask(context, runnable, exceptionHandler, progressDialogTitleResId, progressDialogMessageResId)
                .execute();
    }

    public static void runSlowJob(Activity context, final Runnable runnable, SlowJob.ExceptionHandler exceptionHandler, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
        new SimpleSlowJob(context, runnable, exceptionHandler, progressDialogTitleResId, progressDialogMessageResId).execute();
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

    private static class DefaultExceptionHandler implements SlowAsyncTask.ExceptionHandler {
        private final Context context;

        DefaultExceptionHandler(Context context) {
            this.context = context;
        }

        public void handle(Exception e) {
            Dialogs.alert(context, R.string.error, e.getMessage());
        }
    }
}
