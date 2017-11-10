package org.openforis.collect.android.gui.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.openforis.collect.R;

public class Alerts {

    public static void confirm(Context context, int title, int message,
                               final Runnable runOnPositiveButtonClick) {
        confirm(context, title, message, runOnPositiveButtonClick, null);
    }

    public static void confirm(Context context, int titleKey, int messageKey,
                               final Runnable runOnPositiveButtonClick, final Runnable runOnNegativeButtonClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getResources().getString(titleKey));
        builder.setMessage(context.getResources().getString(messageKey));
        builder.setPositiveButton(R.string.confirm_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                runOnPositiveButtonClick.run();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (runOnNegativeButtonClick != null) {
                    runOnNegativeButtonClick.run();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
