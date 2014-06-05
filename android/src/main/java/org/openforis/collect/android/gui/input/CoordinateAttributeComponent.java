package org.openforis.collect.android.gui.input;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.*;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author Daniel Wiell
 */
public class CoordinateAttributeComponent extends EditTextAttributeComponent<UiCoordinateAttribute> {
    private static Pattern COORDINATE_PATTERN = Pattern.compile("(.*),(.*)");
    private final LocationManager locationManager;
    private final LocationUpdater locationUpdater = new LocationUpdater();
    private ViewHolder vh;

    protected CoordinateAttributeComponent(UiCoordinateAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    protected String attributeValue() {
        return attribute.isEmpty() ? "" : attribute.format();
    }

    protected void updateAttributeValue(String newValue) {
        stopLocationRequest();
        Matcher matcher = COORDINATE_PATTERN.matcher(newValue);
        try {
            if (matcher.find()) {
                double y = Double.parseDouble(matcher.group(1));
                double x = Double.parseDouble(matcher.group(2));
                attribute.setX(x);
                attribute.setY(y);
            } else {
                attribute.setX(null);
                attribute.setY(null);
                vh.selectedCoordinateView.setText("");
            }
        } catch (NumberFormatException ignore) {
            attribute.setX(null);
            attribute.setY(null);
            vh.selectedCoordinateView.setText(context.getResources().getString(R.string.message_invalid_coordinate));
        }
    }

    protected void onEditTextCreated(EditText input) {
        vh = new ViewHolder(input);
    }

    protected View toInputView() {
        return vh.view;
    }

    private void requestLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationUpdater, context.getMainLooper());
    }

    private void stopLocationRequest() {
        locationManager.removeUpdates(locationUpdater);
    }

    private final class LocationUpdater implements LocationListener {
        public void onLocationChanged(Location location) {
            double x = location.getLongitude();
            double y = location.getLatitude();
            float accuracy = location.getAccuracy();
            vh.accuracyView.setText("Accuracy: " + Math.round(accuracy) + "m");
            vh.selectedCoordinateView.setText(y + ", " + x);
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
        LinearLayout gpsLine;
        TextView selectedCoordinateView;
        TextView accuracyView;
        ToggleButton button;

        private ViewHolder(TextView selectedCoordinateView) {
            this.selectedCoordinateView = selectedCoordinateView;
            button = createButton();
            accuracyView = createAccuracyView();

            gpsLine = new LinearLayout(context);
            gpsLine.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            gpsLine.setOrientation(LinearLayout.HORIZONTAL);
            gpsLine.addView(selectedCoordinateView);
            gpsLine.addView(button);

            view = new LinearLayout(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            view.setOrientation(LinearLayout.VERTICAL);
            view.addView(gpsLine);
            view.addView(accuracyView);
        }

        private ToggleButton createButton() {
            ToggleButton button = new ToggleButton(context);
            button.setText("Start GPS"); // TODO: Use message strings
            button.setTextOn("Stop GPS");
            button.setTextOff("Start GPS");
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
    }

}
