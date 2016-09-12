package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import org.openforis.collect.R;

public class ImportingDemoSurveyDialog extends DialogFragment {
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
                        SurveyListActivity activity = (SurveyListActivity) getActivity();
                        ServiceLocator.importDefaultSurvey(activity);
                        activity.startImportedSurveyNodeActivity();
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