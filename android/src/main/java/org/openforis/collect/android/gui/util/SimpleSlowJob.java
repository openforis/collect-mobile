package org.openforis.collect.android.gui.util;

import android.app.Activity;

/**
 * @author Stefano Ricci
 */

public class SimpleSlowJob extends SlowJob<Void, Void, Void> {
   public SimpleSlowJob(Activity context, Runnable runnable, ExceptionHandler exceptionHandler, int progressDialogTitleResId, int progressDialogMessageResId) {
        super(context, runnable, exceptionHandler, progressDialogTitleResId, progressDialogMessageResId);
    }
}