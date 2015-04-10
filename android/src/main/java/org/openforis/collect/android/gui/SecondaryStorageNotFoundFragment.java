package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AppDirs;

public class SecondaryStorageNotFoundFragment extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.storage_not_found_message)
                .setMessage(AppDirs.root(getActivity()).getAbsolutePath())
                .setPositiveButton(R.string.storage_not_found_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SurveyNodeActivity.restartActivity(getActivity());
                    }
                }).setNeutralButton(R.string.storage_not_found_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                showSettings();
            }
        });
        return builder.create();
    }

    private void showSettings() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }
}
