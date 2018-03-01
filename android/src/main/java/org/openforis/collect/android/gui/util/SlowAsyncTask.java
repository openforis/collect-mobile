package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.openforis.collect.R;

/**
 * @author Stefano Ricci
 *
 * It shows a progress dialog while the actual task is running.
 * Excpetion thrown in doInBackground method can be handled by extending the method handleException
 * or by passing an ExceptionHandler to the constructor
 *
 * Subclasses should override the method runTask and handleException
 */
public abstract class SlowAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected final Activity context;
    private final Runnable runnable;
    private final int progressDialogTitleResId;
    private final int progressDialogMessageResId;
    private final ExceptionHandler exceptionHandler;

    protected Status status = Status.INITIALIZING;
    private ProgressDialog progressDialog;
    private Exception lastException;

    enum Status {
        INITIALIZING, RUNNING, COMPLETED, ERROR
    }

    public SlowAsyncTask(Activity context) {
        this(context, R.string.processing, R.string.please_wait);
    }

    public SlowAsyncTask(Activity context, int progressDialogTitleResId,
                         int progressDialogMessageResId) {
        this(context, null, null, progressDialogTitleResId, progressDialogMessageResId);
    }

    public SlowAsyncTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                         int progressDialogMessageResId) {
        this(context, runnable, null, progressDialogTitleResId, progressDialogMessageResId);
    }

    public SlowAsyncTask(Activity context, Runnable runnable, ExceptionHandler exceptionHandler, int progressDialogTitleResId, int progressDialogMessageResId) {
        super();
        this.context = context;
        this.runnable = runnable;
        this.exceptionHandler = exceptionHandler;
        this.progressDialogTitleResId = progressDialogTitleResId;
        this.progressDialogMessageResId = progressDialogMessageResId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, context.getString(progressDialogTitleResId),
                context.getString(progressDialogMessageResId), true);
    }

    @Override
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
        super.onPostExecute(result);
        progressDialog.dismiss();
        progressDialog = null;

        if (status == Status.ERROR) {
            handleException(lastException);
        }
    }

    protected void handleException(Exception e) {
        if (exceptionHandler != null) {
            exceptionHandler.handle(lastException);
        }
    }

    public interface ExceptionHandler {
        void handle(Exception e);
    }
}
