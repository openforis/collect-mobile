package org.openforis.collect.android.gui.components;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.common.base.Strings;

import org.openforis.collect.R;

public class OptionButton extends LinearLayout {

    final static int DESCRIPTION_MAX_LINES = 3;

    public enum DisplayType {
        CHECKBOX, RADIOBUTTON
    }

    CompoundButton button;
    AppCompatTextView labelText;
    AppCompatTextView descriptionText;
    boolean descriptionEmpty;
    DisplayType displayType;

    public OptionButton(Context context, DisplayType displayType) {
        super(context);
        this.displayType = displayType;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.option_button, this);
        initComponents(context);
    }

    private void initComponents(Context context) {
        button = displayType == DisplayType.RADIOBUTTON ? new AppCompatRadioButton(context) : new AppCompatCheckBox(context);
        this.addView(button, 0);
        labelText = findViewById(R.id.label);
        descriptionText = findViewById(R.id.description);
    }

    public void setLabel(String label) {
        labelText.setText(label);
    }

    public void setDescription(String description) {
        descriptionEmpty = Strings.isNullOrEmpty(description);
        descriptionText.setVisibility(descriptionEmpty ? GONE : VISIBLE);
        descriptionText.setText(description);
    }

    public void setChecked(boolean checked) {
        button.setChecked(checked);
        if (!descriptionEmpty) {
            descriptionText.setMaxLines(checked ? Integer.MAX_VALUE : DESCRIPTION_MAX_LINES);
        }
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener listener) {
        super.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                button.setChecked(!button.isChecked());
                listener.onClick(OptionButton.this);
            }
        });
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                listener.onClick(OptionButton.this);
            }
        });
    }

}
