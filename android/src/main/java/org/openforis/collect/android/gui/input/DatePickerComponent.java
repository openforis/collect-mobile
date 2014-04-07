package org.openforis.collect.android.gui.input;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.viewmodel.UiDateAttribute;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
class DatePickerComponent extends AttributeInputComponent<UiDateAttribute> {
    private final DatePicker datePicker;

    DatePickerComponent(UiDateAttribute attribute, Context context) {
        super(attribute, context);
        this.datePicker = createDatePicker();
    }

    public View getView() {
        return datePicker;
    }

    public void updateAttribute() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        Date date = calendar.getTime();
        if (hasChanged(date)) {
            attribute().setDate(date);
            notifyAboutAttributeChange();
        }
    }

    public DatePicker createDatePicker() {
        DatePicker datePicker = new DatePicker(context());
        if (AndroidVersion.greaterThen10())
            configureForNewerAndroids(datePicker);
        Date date = attribute().getDate();
        if (date != null)
            initDatePickerDate(datePicker, date);
        datePicker.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return datePicker;
    }

    private void initDatePickerDate(DatePicker datePicker, Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void configureForNewerAndroids(DatePicker datePicker) {
        datePicker.setSpinnersShown(false);
    }

    private boolean hasChanged(Date date) {
        return !date.equals(attribute().getDate());
    }
}
