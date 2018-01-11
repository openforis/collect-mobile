package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.internal.util.Predicate;

import org.openforis.collect.R;

public class Dialogs {

    public static void confirm(Context context, int titleKey, int messageKey,
                               final Runnable runOnPositiveButtonClick) {
        confirm(context, titleKey, messageKey, runOnPositiveButtonClick, null);
    }

    public static void confirm(Context context, int titleKey, int messageKey,
                               final Runnable runOnPositiveButtonClick, Runnable runOnNegativeButtonClick) {
        confirm(context, titleKey, messageKey, runOnPositiveButtonClick, runOnNegativeButtonClick,
                R.string.confirm_label);
    }

    public static void confirm(Context context, int titleKey, int messageKey,
                               final Runnable runOnPositiveButtonClick, Runnable runOnNegativeButtonClick,
                               int positiveButtonLabelKey) {
        confirm(context, titleKey, messageKey, runOnPositiveButtonClick, runOnNegativeButtonClick,
                positiveButtonLabelKey, android.R.string.cancel);
    }

    public static void confirm(Context context, int titleKey, int messageKey,
                               final Runnable runOnPositiveButtonClick, final Runnable runOnNegativeButtonClick,
                               int positiveButtonLabelKey, int negativeButtonLabelKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getResources().getString(titleKey));
        builder.setMessage(context.getResources().getString(messageKey));
        builder.setPositiveButton(positiveButtonLabelKey, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                runOnPositiveButtonClick.run();
            }
        });
        builder.setNegativeButton(negativeButtonLabelKey, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (runOnNegativeButtonClick != null) {
                    runOnNegativeButtonClick.run();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showProgressDialogWhile(final Activity context, final Predicate<Void> predicate, final Runnable callback) {
        if (predicate.apply(null)) {
            callback.run();
        } else {
            final ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.processing),
                    context.getString(R.string.please_wait), true);
            Runnable predicateVerifier = new Runnable() {
                public void run() {
                    if (predicate.apply(null)) {
                        Tasks.runDelayed(this, 100);
                    } else {
                        progressDialog.dismiss();
                        callback.run();
                    }
                }
            };
            Tasks.runDelayed(predicateVerifier, 100);
        }
    }
}
