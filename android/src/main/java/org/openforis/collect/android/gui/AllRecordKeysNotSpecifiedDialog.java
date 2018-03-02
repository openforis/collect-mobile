package org.openforis.collect.android.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.SurveyExporter;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;
import org.openforis.collect.android.viewmodel.UiSurvey;

import java.util.Iterator;

public class AllRecordKeysNotSpecifiedDialog extends AppCompatDialogFragment {

    public static String generateMessage(Activity context) {
        UiSurvey survey = ServiceLocator.surveyService().selectedNode().getUiSurvey();
        UiRecordCollection recordCollection = (UiRecordCollection) survey.getChildAt(0);
        UiRecord.Placeholder placeHolder = (UiRecord.Placeholder) recordCollection.getChildren().get(0);
        StringBuilder keyAttributes = keyAttributes(placeHolder);

        return String.format(
                context.getText(R.string.all_key_records_not_specified_message).toString(),
                keyAttributes, recordCollection.getLabel()
        );
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TextView message = new TextView(getActivity());
        // TODO: Get record label and key attribute names
        UiSurvey survey = ServiceLocator.surveyService().selectedNode().getUiSurvey();
        UiRecordCollection recordCollection = (UiRecordCollection) survey.getChildAt(0);
        UiRecord.Placeholder placeHolder = (UiRecord.Placeholder) recordCollection.getChildren().get(0);
        String recordLabel = placeHolder.getLabel() == null ? placeHolder.getName() : placeHolder.getLabel();

        final String messageText = generateMessage(getActivity());
        final SpannableString s = new SpannableString(messageText);
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        message.setPadding(16, 16, 16, 16);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        String titleText = String.format(
                getActivity().getText(R.string.all_key_records_not_specified_title).toString(),
                recordLabel
        );
        return new AlertDialog.Builder(getActivity())
                .setTitle(titleText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setView(message)
                .create();
    }

    private static StringBuilder keyAttributes(UiRecord.Placeholder placeHolder) {
        StringBuilder keyAttributes = new StringBuilder();
        for (Iterator<UiAttribute> iterator = placeHolder.getKeyAttributes().iterator(); iterator.hasNext(); ) {
            UiAttribute attribute = iterator.next();
            keyAttributes.append(attribute.getLabel() == null ? attribute.getName() : attribute.getLabel());
            if (iterator.hasNext())
                keyAttributes.append(", ");
        }
        return keyAttributes;
    }
}