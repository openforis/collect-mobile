package org.openforis.collect.android.gui.input;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiBooleanAttribute;

/**
 * @author Daniel Wiell
 */
public class BooleanAttributeComponent extends AttributeComponent<UiBooleanAttribute> {
    private static final int TRUE_ID = 100;
    private static final int FALSE_ID = 50;
    private final RadioGroup radioGroup;
    private Boolean checked;

    protected BooleanAttributeComponent(UiBooleanAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        checked = attribute.getValue();
        radioGroup = createRadioGroup();
    }

    private RadioGroup createRadioGroup() {
        RadioGroup radioGroup = new RadioGroup(context);
        // TODO: Don't hard-code labels
        RadioButton yes = createButton("Yes", TRUE_ID);
        RadioButton no = createButton("No", FALSE_ID);
        radioGroup.addView(yes);

        radioGroup.addView(no);

        if (checked != null && checked) {
            yes.setSelected(true);
            radioGroup.check(TRUE_ID);
        }

        if (checked != null && !checked) {
            no.setSelected(true);
            radioGroup.check(FALSE_ID);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checked = checkedId == TRUE_ID;
                saveNode();
            }
        });
        return radioGroup;
    }

    private RadioButton createButton(String label, int id) {
        RadioButton radioButton = new RadioButton(context);
        radioButton.setText(label);
        radioButton.setId(id);
        return radioButton;
    }

    protected boolean updateAttributeIfChanged() {
        boolean changed = checked != attribute.getValue() || (checked != null && checked.equals(attribute.getValue()));
        if (changed) {
            attribute.setValue(checked);
            return true;
        }
        return false;
    }

    protected View toInputView() {
        return radioGroup;
    }
}
