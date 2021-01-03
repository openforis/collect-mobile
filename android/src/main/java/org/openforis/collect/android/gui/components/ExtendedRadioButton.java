package org.openforis.collect.android.gui.components;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.common.base.Strings;

import org.openforis.collect.R;

public class ExtendedRadioButton extends LinearLayout {

    AppCompatRadioButton radioButton;
    AppCompatTextView labelText;
    AppCompatTextView descriptionText;


    public ExtendedRadioButton(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.extended_radio_button, this);
        initComponents();
    }

    private void initComponents() {
        radioButton = findViewById(R.id.button);
        labelText = findViewById(R.id.label);
        descriptionText = findViewById(R.id.description);
    }

    public void setLabel(String label) {
        labelText.setText(label);
    }

    public void setDescription(String description) {
        descriptionText.setVisibility(Strings.isNullOrEmpty(description) ? GONE : VISIBLE);
        descriptionText.setText(description);
    }

    public void setChecked(boolean checked) {
        radioButton.setChecked(checked);
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener listener) {
        super.setOnClickListener(listener);
        radioButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                listener.onClick(ExtendedRadioButton.this);
            }
        });
    }
}
