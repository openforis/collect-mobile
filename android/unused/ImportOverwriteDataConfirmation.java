package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.openforis.collect.R;

public class ImportOverwriteDataConfirmation extends DialogFragment {

    public static final String ARG_SURVEY_PATH = ImportOverwriteDataConfirmation.class.getName() + ".surveyUri";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.import_overwrite_data_dialog_title)
                .setMessage(R.string.import_overwrite_data_dialog_message)
                .setPositiveButton(R.string.import_overwrite_data_dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((SurveyListActivity) getActivity()).importSurvey(getArguments().getString(ARG_SURVEY_PATH), true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


    public static ImportOverwriteDataConfirmation create(String surveyPath) {
        ImportOverwriteDataConfirmation dialog = new ImportOverwriteDataConfirmation();
        Bundle args = new Bundle();
        args.putString(ARG_SURVEY_PATH, surveyPath);
        dialog.setArguments(args);
        return dialog;
    }
}