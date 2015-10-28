package org.openforis.collect.android.gui.input;

import android.graphics.Paint;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.detail.NavigationDialogFragment;
import org.openforis.collect.android.util.CoordinateUtils;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;

import java.text.NumberFormat;
import java.text.ParseException;

import static android.text.InputType.*;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * @author Daniel Wiell
 */
public class CoordinateAttributeComponent extends AttributeComponent<UiCoordinateAttribute> {
    private final LocationProvider locationProvider;
    private ViewHolder vh;

    protected CoordinateAttributeComponent(UiCoordinateAttribute attribute, SurveyService surveyService, final FragmentActivity context) {
        super(attribute, surveyService, context);
        vh = new ViewHolder();
        locationProvider = new LocationProvider(new UpdateListener(context), context, true);
    }

    protected boolean updateAttributeIfChanged() {
        stopLocationRequest();
        UiSpatialReferenceSystem srs = selectedSpatialReferenceSystem();
        Double x = toDouble(vh.xView);
        Double y = toDouble(vh.yView);

        if (notEqual(srs, attribute.getSpatialReferenceSystem()) ||
                notEqual(x, attribute.getX()) ||
                notEqual(y, attribute.getY())) {
            attribute.setSpatialReferenceSystem(srs);
            attribute.setX(x);
            attribute.setY(y);
            return true;
        }
        return false;
    }

    private UiSpatialReferenceSystem selectedSpatialReferenceSystem() {
        return (UiSpatialReferenceSystem) vh.srsSpinner.getSelectedItem();
    }

    private Double toDouble(TextView textView) {
        if (textView.getText().toString().isEmpty())
            return null;
        try {
            return numberFormat().parse(textView.getText().toString()).doubleValue();
        } catch (ParseException e) {
            textView.setError(context.getResources().getString(R.string.message_not_a_number));
        }
        return null;
    }

    protected final View toInputView() {
        return vh.view;
    }

    private void requestLocation() {
        locationProvider.start();
    }

    private void stopLocationRequest() {
        locationProvider.stop();
        vh.button.setChecked(false);
    }

    private double[] transformToSelectedSrs(Location location) {
        double[] coord = new double[]{location.getLongitude(), location.getLatitude()};
        UiSpatialReferenceSystem from = UiSpatialReferenceSystem.LAT_LNG_SRS;
        UiSpatialReferenceSystem to = selectedSpatialReferenceSystem();
        return CoordinateUtils.transform(from, coord, to);
    }

    private NumberFormat numberFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(10);
        numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return numberFormat;
    }

    private class ViewHolder {
        LinearLayout view;
        Spinner srsSpinner;
        TextView xView;
        TextView yView;
        TextView accuracyView;
        ToggleButton button;
        Button navigateButton;
        ArrayAdapter<UiSpatialReferenceSystem> adapter;

        private ViewHolder() {
            srsSpinner = createSrsSpinner();
            LinearLayout srsLayout = createSrsLayout();
            this.xView = createNumberInput(attribute.getX(), "x");
            this.yView = createNumberInput(attribute.getY(), "y");
            button = createButton();
            accuracyView = createAccuracyView();

            view = new LinearLayout(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            view.setOrientation(LinearLayout.VERTICAL);

            view.addView(srsLayout);
            view.addView(xView);
            view.addView(yView);
            view.addView(button);
            if (attribute.getDefinition().destinationPointSpecified) {
                navigateButton = createNavigationButton();
                view.addView(navigateButton);
            }

            view.addView(accuracyView);
        }

        private LinearLayout createSrsLayout() {
            TextView srsLabel = new TextView(context);
            srsLabel.setText(context.getResources().getString(R.string.label_spatial_reference_system) + ":");

            LinearLayout srsLine = new LinearLayout(context);
            srsLine.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            srsLine.setOrientation(LinearLayout.HORIZONTAL);
            srsLine.addView(srsLabel);
            srsLine.addView(srsSpinner);
            return srsLine;
        }

        private Spinner createSrsSpinner() {
            final Spinner srsSpinner = new Spinner(context);
            adapter = new ArrayAdapter<UiSpatialReferenceSystem>(context,
                    android.R.layout.simple_spinner_dropdown_item,
                    attribute.getDefinition().spatialReferenceSystems);
            srsSpinner.setAdapter(adapter);
            selectCurrentSrsInSpinner(srsSpinner);
            srsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    saveNode();
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    saveNode();
                }
            });
            return srsSpinner;
        }

        private Button createNavigationButton() {
            Button button = new Button(context);
            button.setTextAppearance(context, android.R.style.TextAppearance_Small);
            button.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setText(context.getResources().getString(R.string.label_navigate));
            button.setBackgroundDrawable(null);
            button.setPaintFlags(button.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    NavigationDialogFragment.show(context.getSupportFragmentManager());
                }
            });
            int linkColor = new TextView(context).getLinkTextColors().getDefaultColor();
            button.setTextColor(linkColor);
            return button;
        }

        private void selectCurrentSrsInSpinner(Spinner srsSpinner) {
            UiSpatialReferenceSystem selectedSrs = attribute.getSpatialReferenceSystem();
            if (selectedSrs != null) {
                int position = attribute.getDefinition().spatialReferenceSystems.indexOf(selectedSrs);
                srsSpinner.setSelection(position);
            }
        }

        private TextView createNumberInput(Double value, String hint) {
            final TextView input = new EditText(context);
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                        saveNode();
                }
            });
            input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT)
                        saveNode();
                    return false;
                }
            });
            input.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void afterTextChanged(Editable s) {
                    input.setError(null);
                }
            });
            input.setSingleLine();
            if (value != null)
                input.setText(formatDouble(value));
            input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint(hint);
            return input;
        }

        private ToggleButton createButton() {
            ToggleButton button = new ToggleButton(context);
            button.setText(context.getResources().getString(R.string.label_start_gps));
            button.setTextOn(context.getResources().getString(R.string.label_stop_gps));
            button.setTextOff(context.getResources().getString(R.string.label_start_gps));
            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        requestLocation();
                    else {
                        stopLocationRequest();
                        saveNode();
                    }

                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                }
            });
            return button;
        }

        private TextView createAccuracyView() {
            return new TextView(context);
        }

        public void updateCoordinate(double[] transformedCoord) {
            xView.setText(formatDouble(transformedCoord[0]));
            yView.setText(formatDouble(transformedCoord[1]));
        }

        private String formatDouble(Double value) {
            return numberFormat().format(value);
        }
    }

    private class UpdateListener implements LocationProvider.LocationUpdateListener {
        private final FragmentActivity context;

        public UpdateListener(FragmentActivity context) {this.context = context;}

        public void onUpdate(Location location) {
            float accuracy = location.getAccuracy();
            vh.accuracyView.setText(context.getResources().getString(R.string.label_accuracy) + ": " + Math.round(accuracy) + "m");
            double[] transformedCoord = transformToSelectedSrs(location);
            vh.updateCoordinate(transformedCoord);
        }
    }
}
