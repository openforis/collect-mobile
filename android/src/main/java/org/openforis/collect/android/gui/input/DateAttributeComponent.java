package org.openforis.collect.android.gui.input;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.UiDateAttribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public class DateAttributeComponent extends EditTextAttributeComponent<UiDateAttribute> {
    private final LinearLayout view;
    private TextView selectedDateView;

    protected DateAttributeComponent(UiDateAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        view = new LinearLayout(context);
        view.addView(selectedDateView);
        view.addView(createButton());
        view.setOrientation(LinearLayout.HORIZONTAL);
    }

    protected boolean hasChanged(String newValue) {
        if (newValue == null)
            return attribute.getDate() != null;
        return !newValue.equals(attributeValue());
    }

    protected String attributeValue() {
        Date date = attribute.getDate();
        return date == null ? "" : format(date);
    }

    protected void updateAttributeValue(String newValue) {
        try {
            attribute.setDate(newValue == null ? null : parse(newValue));
        } catch (ParseException ignore) {
            attribute.setDate(null);
            selectedDateView.setText(context.getResources().getString(R.string.message_invalid_date));
        }
    }

    protected void onEditTextCreated(EditText input) {
        selectedDateView = input;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        selectedDateView.setLayoutParams(params);
    }

    protected View toInputView() {
        return view;
    }

    private void selectDate(Date date) {
        selectedDateView.setText(format(date));
    }

    private View createButton() {
        ImageButton button = new ImageButton(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        button.setImageResource(R.drawable.ic_action_go_to_today);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveNode();
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(context.getSupportFragmentManager(), "datePicker");

            }
        });
        return button;
    }

    private String format(Date date) {
        return new SimpleDateFormat(UiDateAttribute.DATE_PATTERN).format(date);
    }

    private Date parse(String newValue) throws ParseException {
        return new SimpleDateFormat(UiDateAttribute.DATE_PATTERN).parse(newValue);
    }


    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Date date = attribute.getDate();
            Calendar c = Calendar.getInstance();
            if (date != null)
                c.setTime(date);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            if (AndroidVersion.greaterThen10())
                configureForNewerAndroids(dialog);

            return dialog;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void configureForNewerAndroids(DatePickerDialog dialog) {
            DatePicker datePicker = dialog.getDatePicker();
            if (datePicker == null)
                throw new IllegalStateException("Dialog contains no date picker: " + dialog);
            datePicker.setSpinnersShown(false);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            selectDate(c.getTime());
            saveNode();
        }
    }
}
