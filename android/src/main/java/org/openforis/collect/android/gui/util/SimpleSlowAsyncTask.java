package org.openforis.collect.android.gui.util;

import android.app.Activity;

/**
 * @author Stefano Ricci
 */

public class SimpleSlowAsyncTask extends SlowAsyncTask<Void, Void, Void> {

    public SimpleSlowAsyncTask(Activity context) {
        super(context);
    }

    public SimpleSlowAsyncTask(Activity context, int progressDialogTitleResId,
                               int progressDialogMessageResId) {
        super(context, progressDialogTitleResId, progressDialogMessageResId);
    }

    public SimpleSlowAsyncTask(Activity context, Runnable runnable, int progressDialogTitleResId,
                               int progressDialogMessageResId) {
        super(context, runnable, progressDialogTitleResId, progressDialogMessageResId);
    }

    public SimpleSlowAsyncTask(Activity context, Runnable runnable, ExceptionHandler exceptionHandler, int progressDialogTitleResId, int progressDialogMessageResId) {
        super(context, runnable, exceptionHandler, progressDialogTitleResId, progressDialogMessageResId);
    }
}