package org.openforis.collect.android.gui.input;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public class TimeAttributeComponent extends EditTextAttributeComponent<UiTimeAttribute> {
    private static Pattern TIME_PATTERN = Pattern.compile("(\\d?\\d):(\\d?\\d)");
    private final LinearLayout view;
    private TextView selectedTimeView;

    protected TimeAttributeComponent(UiTimeAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        view = new LinearLayout(context);
        view.addView(selectedTimeView);
        view.addView(createButton());
        view.setOrientation(LinearLayout.HORIZONTAL);
    }

    protected String attributeValue() {
        return attribute.format();
    }

    protected void updateAttributeValue(String newValue) {
        Matcher matcher = TIME_PATTERN.matcher(newValue);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            attribute.setTime(hour, minute);
        } else {
            attribute.setTime(null, null);
            selectedTimeView.setText("");
        }
    }

    protected void onEditTextCreated(EditText input) {
        selectedTimeView = input;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        selectedTimeView.setLayoutParams(params);
    }

    protected View toInputView() {
        return view;
    }

    private View createButton() {
        ImageButton button = new ImageButton(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        button.setImageResource(new Attrs(context).resourceId(R.attr.timeIcon));
//
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveNode();
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(context.getSupportFragmentManager(), "timePicker");

            }
        });
        return button;
    }

    private void selectTime(int hour, int minute) {
        selectedTimeView.setText(UiTimeAttribute.format(hour, minute));
    }

    private class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = attribute.getHour() == null ? 0 : attribute.getHour();
            int minute = attribute.getMinute() == null ? 0 : attribute.getMinute();
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            selectTime(hourOfDay, minute);
            saveNode();
        }
    }
}