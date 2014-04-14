package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiTimeAttribute;
import org.openforis.collect.android.viewmodel.UiValidationError;

import java.util.Set;

/**
 * @author Daniel Wiell
 */
public class TimeAttributeComponent extends AttributeComponent<UiTimeAttribute> {
    private final LinearLayout view;
    private final TextView selectedTimeView;

    protected TimeAttributeComponent(UiTimeAttribute attribute, SurveyService surveyService, Context context) {
        super(attribute, surveyService, context);
        view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        selectedTimeView = createSelectedTimeView();
        selectedTimeView.setFocusable(true);
        view.addView(selectedTimeView);
    }

    protected boolean updateAttributeIfChanged() {
        return false;
    }

    protected View toInputView() {
        return view;
    }

    public View getDefaultFocusedView() {
        return selectedTimeView;
    }

    private TextView createSelectedTimeView() {
        TextView selectedTimeView = new TextView(context);
        selectedTimeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    saveNode();
            }
        });
        selectedTimeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    saveNode();
                return false;
            }
        });
        selectedTimeView.setText(attributeStringValue());
        selectedTimeView.setSingleLine();
        return selectedTimeView;
    }

    private String attributeStringValue() {
        return attribute.getHour() + ":" + attribute.getMinute();
    }
}
