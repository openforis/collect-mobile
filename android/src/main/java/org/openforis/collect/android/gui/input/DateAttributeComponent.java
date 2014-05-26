package org.openforis.collect.android.gui.input;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiDateAttribute;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.openforis.collect.android.viewmodel.UiDateAttribute.format;
import static org.openforis.collect.android.viewmodel.UiDateAttribute.parse;

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
        button.setImageResource(new Attrs(context).resourceId(R.attr.goToTodayIcon));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveNode();
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setComponent(DateAttributeComponent.this);
                newFragment.show(context.getSupportFragmentManager(), "datePicker");

            }
        });
        return button;
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private DateAttributeComponent component;

        public void setComponent(DateAttributeComponent component) {
            this.component = component;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Date date = component.attribute.getDate();
            Calendar c = Calendar.getInstance();
            if (date != null)
                c.setTime(date);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            component.selectDate(c.getTime());
            component.saveNode();
        }
    }
}
