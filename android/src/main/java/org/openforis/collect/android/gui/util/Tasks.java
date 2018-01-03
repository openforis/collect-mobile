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
        new SlowProcessTask(context, runnable, progressDialogTitleResId, progressDialogMessageResId)
                .execute();
    }

    public static void runDelayed(final Runnable runnable, int delay) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                runnable.run();
            }
        }, delay);
    }

    private static class SlowProcessTask extends AsyncTask<Void, Void, Void> {

        private final Activity context;
        private final Runnable runnable;
        private final int progressDialogTitleResId;
        private final int progressDialogMessageResId;
        private ProgressDialog progressDialog;

        SlowProcessTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                        int progressDialogMessageResId) {
            super();
            this.context = context;
            this.runnable = runnable;
            this.progressDialogTitleResId = progressDialogTitleResId;
            this.progressDialogMessageResId = progressDialogMessageResId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, context.getString(progressDialogTitleResId),
                    context.getString(progressDialogMessageResId), true);
        }

        protected Void doInBackground(Void... voids) {
            runnable.run();
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }
}
