package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.barcode.BarcodeCaptureActivity;
import org.openforis.collect.android.viewmodel.UiTextAttribute;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Stefano Ricci
 */
public class BarcodeTextAttributeComponent extends TextAttributeComponent {

    private View view;
    private boolean editEnabled = false;

    public BarcodeTextAttributeComponent(UiTextAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected void initializeInputView() {
        view = context.getLayoutInflater().inflate(R.layout.barcode_attribute, null);

        Button captureBtn = view.findViewById(R.id.barcode_capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                              ((SurveyNodeActivity) getContext()).setBarcodeCaptureListener(BarcodeTextAttributeComponent.this);
                                              // launch barcode capture activity.
                                              Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                                              intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                                              intent.putExtra(BarcodeCaptureActivity.UseFlash, true);
                                              getContext().startActivityForResult(intent, SurveyNodeActivity.BARCODE_CAPTURE_REQUEST_CODE);
                                          }
                                      }
        );
        initializeEditText();

        LinearLayout textContainer = view.findViewById(R.id.barcode_text_container);
        textContainer.addView(editText, 0);

        initializeEnableTextEditSwitch();
    }

    private void initializeEnableTextEditSwitch() {
        Switch textEditSwitch = view.findViewById(R.id.barcode_text_edit_switch);
        textEditSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editEnabled = isChecked;
                updateViewState();
            }
        });
    }

    @Override
    protected EditText initializeEditText() {
        EditText editText = super.initializeEditText();
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
        editText.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        editText.setEnabled(false);
        return editText;
    }

    @Override
    protected View toInputView() {
        return view;
    }

    @Override
    protected TextView errorMessageContainerView() {
        return getEditText();
    }

    @Override
    protected void onEditTextCreated(EditText input) {
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void barcodeCaptured(Barcode barcode) {
        editText.setText(barcode.displayValue);
        saveNode();
    }

    private void updateViewState() {
        editText.setEnabled(editEnabled);
    }
}
