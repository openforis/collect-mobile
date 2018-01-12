package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.internal.util.Predicate;

import org.openforis.collect.R;

public class Dialogs {

    public static void alert(Context context, int titleKey, int messageKey) {
        alert(context, titleKey, messageKey, null);
    }

    public static void alert(Context context, int titleKey, int messageKey, final Runnable runOnPositiveButtonClick) {
        alert(context, context.getResources().getString(titleKey), context.getResources().getString(messageKey), runOnPositiveButtonClick);
    }

    public static void alert(Context context, String title, String message) {
        alert(context, title, message, null);
    }

    public static void alert(Context context, String title, String message, final Runnable runOnPositiveButtonClick) {
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (runOnPositiveButtonClick != null) {
                        runOnPositiveButtonClick.run();
                    }
                }
            })
            .create();
        dialog.show();
    }

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
        AlertDialog dialog = new AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(context.getResources().getString(titleKey))
            .setMessage(context.getResources().getString(messageKey))
            .setPositiveButton(positiveButtonLabelKey, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    runOnPositiveButtonClick.run();
                }
            })
            .setNegativeButton(negativeButtonLabelKey, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (runOnNegativeButtonClick != null) {
                        runOnNegativeButtonClick.run();
                    }
                }
            })
            .create();
        dialog.show();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog dialog = ProgressDialog.show(context, context.getString(R.string.processing),
                context.getString(R.string.please_wait), true);
        return dialog;
    }

    public static void showProgressDialogWhile(final Context context, final Predicate<Void> predicate, final Runnable callback) {
        if (predicate.apply(null)) {
            callback.run();
        } else {
            final ProgressDialog progressDialog = showProgressDialog(context);
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
