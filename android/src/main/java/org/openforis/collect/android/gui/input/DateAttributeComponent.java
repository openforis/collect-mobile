package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiDateAttribute;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Wiell
 */
public class DateAttributeComponent extends AttributeComponent<UiDateAttribute> {
    private static final String DATE_PATTERN = "dd MMMM yyyy";
    private final LinearLayout view;
    private final TextView selectedDateView;

    protected DateAttributeComponent(UiDateAttribute attribute, SurveyService surveyService, Context context) {
        super(attribute, surveyService, context);
        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        selectedDateView = createSelectedDateView();
        view.addView(selectedDateView);
    }

    protected boolean updateAttributeIfChanged() {
        return false;
    }

    protected View toInputView() {
        return view;
    }

    private TextView createSelectedDateView() {
        TextView editText = new TextView(context);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    saveNode();
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    saveNode();
                return false;
            }
        });
        editText.setText(attributeStringValue());
        editText.setSingleLine();
        return editText;
    }

    private String attributeStringValue() {
        Date date = attribute.getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return date == null ? null : dateFormat.format(date);
    }
}
