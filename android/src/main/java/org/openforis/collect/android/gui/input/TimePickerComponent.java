package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;

import java.util.Calendar;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
class TimePickerComponent extends InputComponent<UiTimeAttribute> {
    private final TimePicker timePicker;

    TimePickerComponent(UiTimeAttribute attribute, Context context) {
        super(attribute, context);
        this.timePicker = createTimePicker();
    }

    public View getView() {
        return timePicker;
    }

    public void updateAttribute() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        if (hasChanged(hour, minute)) {
            attribute().setTime(hour, minute);
            notifyAboutAttributeChange();
        }
    }

    public TimePicker createTimePicker() {
        TimePicker timePicker = new TimePicker(context());
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context()));
        timePicker.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        final Calendar c = Calendar.getInstance();
        timePicker.setCurrentHour(attribute().getHour());
        timePicker.setCurrentMinute(attribute().getMinute());
        return timePicker;
    }

    private boolean hasChanged(int hour, int minute) {
        return attribute().getHour() != hour || attribute().getMinute() != minute;
    }
}
