package org.openforis.collect.android.gui.input;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;

import java.util.Calendar;
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
        newValue = newValue == null ? "" : newValue;
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

    @Override
    protected void onEditTextCreated(EditText input) {
        super.onEditTextCreated(input);
        selectedTimeView = input;
        selectedTimeView.setHint(context.getResources().getString(R.string.hint_time_pattern) + " ");
        selectedTimeView.setInputType(InputType.TYPE_NULL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        selectedTimeView.setLayoutParams(params);
        selectedTimeView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTimePicker();
            }
        });
    }

    protected View toInputView() {
        return view;
    }

    private void openTimePicker() {
        saveNode();
        hideKeyboard();
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setComponent(TimeAttributeComponent.this);
        newFragment.show(context.getSupportFragmentManager(), "timePicker");
    }

    private View createButton() {
        ImageButton button = new AppCompatImageButton(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        button.setImageResource(new Attrs(context).resourceId(R.attr.timeIcon));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               openTimePicker();
            }
        });
        return button;
    }

    private void selectTime(int hour, int minute) {
        selectedTimeView.setText(UiTimeAttribute.format(hour, minute));
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private TimeAttributeComponent component;

        public void setComponent(TimeAttributeComponent component) {
            this.component = component;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            UiTimeAttribute attribute = component.attribute;
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            int hour = attribute.getHour() == null ? currentHour : attribute.getHour();
            int minute = attribute.getMinute() == null ? currentMinute : attribute.getMinute();
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            component.selectTime(hourOfDay, minute);
            component.saveNode();
        }
    }
}