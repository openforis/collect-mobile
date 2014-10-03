package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ImportOverwriteDataConfirmation extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage("Data you entered for current survey will be deleted if you proceed to import a survey.")
                .setPositiveButton("Delete data and import", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((SurveyNodeActivity) getActivity()).showImportDialog();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }


}