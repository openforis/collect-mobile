package org.openforis.collect.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.settings.SettingsActivity;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AppDirs;

public class WorkingDirectoryNotAccessibleFragment extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.working_directory_not_accessible_message)
                .setMessage(AppDirs.root(getActivity()).getAbsolutePath())
                .setPositiveButton(R.string.storage_not_found_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Activities.start(getActivity(), MainActivity.class);
                    }
                }).setNeutralButton(R.string.storage_not_found_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Activities.start(getActivity(), SettingsActivity.class);
                    }
        });
        return builder.create();
    }
}
