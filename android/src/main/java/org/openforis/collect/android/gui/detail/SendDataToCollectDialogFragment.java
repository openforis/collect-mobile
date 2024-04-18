package org.openforis.collect.android.gui.detail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.SubmitDataToCollectActivity;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecordCollection;

import java.util.Arrays;
import java.util.List;

public class SendDataToCollectDialogFragment extends DialogFragment {
    private static final int ALL_RECORDS = 0;
    private static final int ONLY_CURRENT_RECORD = 1;

    private Dialog dialog = null;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SurveyService surveyService = ServiceLocator.surveyService();

        final int[] checkedItem = {ALL_RECORDS};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.submit_to_collect_confirm_title)
                .setPositiveButton(R.string.action_submit_data_to_collect, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (SendDataToCollectDialogFragment.this.dialog != null) {
                            SendDataToCollectDialogFragment.this.dialog.dismiss();
                        }
                        Intent intent = new Intent(getContext(), SubmitDataToCollectActivity.class);
                        boolean onlyCurrentRecord = checkedItem[0] == ONLY_CURRENT_RECORD;
                        if (onlyCurrentRecord) {
                            int recordId = surveyService.selectedNode().getUiRecord().getId();
                            intent.putExtra(SubmitDataToCollectActivity.EXTRA_ONLY_RECORD_IDS, new int[]{recordId});
                        }
                        getContext().startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        UiNode selectedNode = surveyService.selectedNode();
        if (selectedNode instanceof UiRecordCollection) {
            builder.setMessage(R.string.submit_to_collect_confirm_message);
        } else {
            List<String> options = Arrays.asList(
                getString(R.string.export_dialog_option_all_records),
                getString(R.string.export_dialog_option_only_current_record)
            );
            builder.setSingleChoiceItems(options.toArray(new String[0]), checkedItem[0], new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    checkedItem[0] = which;
                }
            });
        }
        dialog = builder.create();
        return dialog;
    }

}