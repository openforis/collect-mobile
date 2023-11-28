package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.app.ProgressDialog;

import org.openforis.collect.R;

public class SlowJob<Params, Progress, Result> {

    enum Status {
        PENDING, RUNNING, COMPLETED, ERROR
    }

    protected final Activity context;
    private final Runnable runnable;
    private final int progressDialogTitleResId;
    private final int progressDialogMessageResId;
    private final ExceptionHandler exceptionHandler;

    protected Status status = Status.PENDING;
    private ProgressDialog progressDialog;
    private Exception lastException;

    public SlowJob(Activity context, Runnable runnable, int progressDialogTitleResId, int progressDialogMessageResId) {
        this(context, runnable, null, progressDialogTitleResId, progressDialogMessageResId);
    }

    public SlowJob(Activity context, Runnable runnable, ExceptionHandler exceptionHandler, int progressDialogTitleResId, int progressDialogMessageResId) {
        super();
        this.context = context;
        this.runnable = runnable;
        this.exceptionHandler = exceptionHandler;
        this.progressDialogTitleResId = progressDialogTitleResId;
        this.progressDialogMessageResId = progressDialogMessageResId;
    }

    public void execute() {
        onPreExecute();

        Result result = doInBackground();

        onPostExecute(result);
    }

    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, context.getString(progressDialogTitleResId),
                context.getString(progressDialogMessageResId), true);
    }

    protected Result doInBackground(Params... params) {
        status = Status.RUNNING;
        try {
            Result result = runTask();
            status = Status.COMPLETED;
            return result;
        } catch (Exception e) {
            lastException = e;
            status = Status.ERROR;
        }
        return null;
    }

    protected Result runTask() throws Exception {
        if (runnable != null) {
            runnable.run();
        }
        return null;
    }

    protected void onPostExecute(Result result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (status == Status.ERROR) {
            handleException(lastException);
        }
    }

    protected void handleException(Exception e) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(lastException);
        }
    }

    protected void showWarning(final int messageKey) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialogs.alert(context, R.string.warning, messageKey);
            }
        });
    }

    public interface ExceptionHandler {
        void handle(Exception e);
    }
}
