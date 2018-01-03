package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.openforis.collect.R;

import java.util.Timer;
import java.util.TimerTask;

public class Tasks {

    public static void runSlowTask(Activity context, Runnable runnable) {
        runSlowTask(context, runnable, R.string.processing, R.string.please_wait);
    }

    public static void runSlowTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                                   int progressDialogMessageResId) {
        ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(progressDialogTitleResId),
                context.getString(progressDialogMessageResId), true);
        new SlowProcessTask(context, runnable, progressDialog).execute();
    }

    public static void runDelayed(final Runnable runnable, int delay) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                runnable.run();
            }
        }, delay);
    }

    public static void runDelayedOnUiThread(final Activity context, final Runnable runnable, int delay) {
        runDelayed(new Runnable() {
            public void run() {
                context.runOnUiThread(runnable);
            }
        }, delay);
    }

    private static class SlowProcessTask extends AsyncTask<Void, Void, Void> {

        private final Activity context;
        private final Runnable runnable;
        private final ProgressDialog progressDialog;

        SlowProcessTask(Activity context, Runnable runnable, ProgressDialog progressDialog) {
            super();
            this.context = context;
            this.runnable = runnable;
            this.progressDialog = progressDialog;
        }

        protected Void doInBackground(Void... voids) {
            runnable.run();

            //dismiss the progress dialog after 1 second
            runDelayedOnUiThread(context, new Runnable() {
                public void run() {
                    progressDialog.dismiss();
                }
            }, 1000);

            return null;
        }
    }
}
