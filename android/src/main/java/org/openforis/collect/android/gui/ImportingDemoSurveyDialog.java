package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Tasks;

public class ImportingDemoSurveyDialog extends DialogFragment {

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TextView message = new TextView(getActivity());
        final SpannableString s =
                new SpannableString(getActivity().getText(R.string.import_demo_dialog_message));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());


        message.setPadding(px(8), px(16), px(8), px(16));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.import_demo_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final SurveyListActivity activity = (SurveyListActivity) getActivity();
                        Tasks.runSlowTask(activity, new Runnable() {
                            public void run() {
                                ServiceLocator.importDefaultSurvey(activity);
                                SurveyNodeActivity.startClearSurveyNodeActivity(activity);
                            }
                        }, R.string.import_demo_processing_title, R.string.please_wait);
                    }
                })
                .setView(message)
                .create();
    }

    private int px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}