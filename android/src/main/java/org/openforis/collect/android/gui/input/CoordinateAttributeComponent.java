package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
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
    private final LocationManager locationManager;
    private final LocationUpdater locationUpdater = new LocationUpdater();
    private ViewHolder vh;

    protected CoordinateAttributeComponent(UiCoordinateAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        vh = new ViewHolder();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
        locationUpdater.bestAccuracy = Float.MAX_VALUE;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        for (String provider : locationManager.getAllProviders()) {
            if (locationManager.isProviderEnabled(provider))
                locationManager.requestLocationUpdates(provider, 1000, 0, locationUpdater, context.getMainLooper());
        }
    }

    private void stopLocationRequest() {
        locationManager.removeUpdates(locationUpdater);
        vh.button.setChecked(false);
    }

    private final class LocationUpdater implements LocationListener {
        private float bestAccuracy = Float.MAX_VALUE;

        public synchronized void onLocationChanged(Location location) {
            float accuracy = location.getAccuracy();
            if (accuracy < bestAccuracy) {
                bestAccuracy = accuracy;
                vh.accuracyView.setText(context.getResources().getString(R.string.label_accuracy) + ": " + Math.round(accuracy) + "m");
                double[] transformedCoord = transformToSelectedSrs(location);
                vh.updateCoordinate(transformedCoord);
            }
        }

        private double[] transformToSelectedSrs(Location location) {
            double[] coord = new double[]{location.getLongitude(), location.getLatitude()};
            UiSpatialReferenceSystem from = UiSpatialReferenceSystem.LAT_LNG_SRS;
            UiSpatialReferenceSystem to = selectedSpatialReferenceSystem();
            return CoordinateUtils.transform(from, coord, to);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Do nothing
        }

        public void onProviderEnabled(String provider) {
            // Do nothing
        }

        public void onProviderDisabled(String provider) {
            // Do nothing
        }
    }

    private class ViewHolder {
        LinearLayout view;
        Spinner srsSpinner;
        TextView xView;
        TextView yView;
        TextView accuracyView;
        ToggleButton button;
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
                    if (actionId == EditorInfo.IME_ACTION_DONE)
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

    private NumberFormat numberFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(10);
        numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return numberFormat;
    }

}
