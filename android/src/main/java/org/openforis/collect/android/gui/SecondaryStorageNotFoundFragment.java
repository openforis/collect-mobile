package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import org.openforis.collect.R;

public class SecondaryStorageNotFoundFragment extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.storage_not_found_message)
                .setPositiveButton(R.string.storage_not_found_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SurveyNodeActivity.restartActivity(getActivity());
                    }
                }).setNeutralButton(R.string.storage_not_found_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                updatePreferences();
                SurveyNodeActivity.restartActivity(getActivity());
            }
        });
        return builder.create();
    }

    private void updatePreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useSecondaryStorage", false);
        editor.commit();
    }
}
