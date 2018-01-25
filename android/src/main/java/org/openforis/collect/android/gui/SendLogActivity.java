package org.openforis.collect.android.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Dialogs;


public class SendLogActivity extends Activity implements View.OnClickListener {

    private static final String EMAIL_ADDRESS = "openforislogs" + "@" + "gmail.com";
    private static final String EMAIL_SUBJECT = "Open Foris Collect Mobile - ERROR";
    public static final String LOGS_INTENT_EXTRA = "logs";

    private String logs;

    public static void startActivity(Context context, String logs) {
        Intent intent = new Intent(context, SendLogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra(LOGS_INTENT_EXTRA, logs);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.send_log);
        logs = Activities.getIntentExtra(this, LOGS_INTENT_EXTRA);

        showConfirmDialog();
    }

    public void onClick(View v) {
        // respond to button clicks in your UI
    }

    private void sendLogFile() {
        sendEmail(EMAIL_ADDRESS, EMAIL_SUBJECT, logs);
    }

    private void showConfirmDialog() {
        Dialogs.confirm(SendLogActivity.this, R.string.report_error_dialog_title, R.string.report_error_dialog_message,
                new Runnable() {
                    public void run() {
                        sendLogFile();
                        finish();
                        CollectMobileApplication.exit(SendLogActivity.this);
                    }
                }, new Runnable() {
                    public void run() {
                        finish();
                        CollectMobileApplication.exit(SendLogActivity.this);
                    }
                }, R.string.report_error_dialog_confirm_button);
    }

    private void sendEmail(String address, String subject, String bodyText) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        ComponentName emailApp = emailIntent.resolveActivity(getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        boolean hasEmailApp = emailApp != null && !emailApp.equals(unsupportedAction);

        if (hasEmailApp) {
            String mailto = "mailto:" + Uri.encode(address) +
                            "?subject=" + Uri.encode(subject) +
                            "&body=" + Uri.encode(bodyText);

            emailIntent.setData(Uri.parse(mailto));

            startActivity(emailIntent);
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("plain/text");
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {EMAIL_ADDRESS});
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse ("file://" + logFilePath));
            shareIntent.putExtra(Intent.EXTRA_TEXT, logs);
            startActivity(shareIntent);
        }
    }
}