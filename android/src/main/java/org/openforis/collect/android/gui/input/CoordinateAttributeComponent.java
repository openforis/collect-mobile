package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.detail.NavigationDialogFragment;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.util.CoordinateUtils;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

/**
 * @author Daniel Wiell
 */
public class CoordinateAttributeComponent extends AttributeComponent<UiCoordinateAttribute> {
    private final LocationProvider locationProvider;
    private ViewHolder vh;
    private boolean requestingLocation;

    CoordinateAttributeComponent(UiCoordinateAttribute attribute, SurveyService surveyService, final FragmentActivity context) {
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
        vh.showMapButton.setEnabled(!attribute.isEmpty());
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
        requestingLocation = true;
        Activities.keepScreenOn(context);
    }

    private void stopLocationRequest() {
        locationProvider.stop();
        vh.startStopButton.setChecked(false);
        requestingLocation = false;
        Activities.clearKeepScreenOn(context);
    }

    private double[] transformToSelectedSrs(Location location) {
        double[] coord = new double[]{location.getLongitude(), location.getLatitude()};
        UiSpatialReferenceSystem from = UiSpatialReferenceSystem.LAT_LNG_SRS;
        UiSpatialReferenceSystem to = selectedSpatialReferenceSystem();
        return CoordinateUtils.transform(from, coord, to);
    }

    private double[] transformToLonLat(double x, double y) {
        return CoordinateUtils.transform(selectedSpatialReferenceSystem(),
                new double[]{x, y},
                UiSpatialReferenceSystem.LAT_LNG_SRS);
    }

    private NumberFormat numberFormat() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(10);
        numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);
        return numberFormat;
    }

    @Override
    protected void focusOnMessageContainerView() {
        if (! (vh.xView.hasFocus() || vh.yView.hasFocus())) {
            super.focusOnMessageContainerView();
        }
    }

    private class ViewHolder {
        LinearLayout view;
        Spinner srsSpinner;
        TextView xView;
        TextView yView;
        TextView accuracyView;
        ToggleButton startStopButton;
        Button navigateButton;
        Button showMapButton;
        ArrayAdapter<UiSpatialReferenceSystem> adapter;

        private ViewHolder() {
            srsSpinner = createSrsSpinner();
            LinearLayout srsLayout = createSrsLayout();
            if (attribute.getDefinition().onlyChangedByDevice) {
                this.xView = createNumberOutput(attribute.getX());
                this.yView = createNumberOutput(attribute.getY());
            } else {
                this.xView = createNumberInput(attribute.getX(), "x");
                this.yView = createNumberInput(attribute.getY(), "y");
            }
            startStopButton = createStartStopButton();
            accuracyView = createAccuracyView();
            showMapButton = createShowMapButton();

            view = new LinearLayout(context);
            view.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            view.setOrientation(LinearLayout.VERTICAL);

            view.addView(srsLayout);
            view.addView(xView);
            view.addView(yView);
            view.addView(startStopButton);

            view.addView(accuracyView);

            RelativeLayout belowBar = new RelativeLayout(context);
            RelativeLayout.LayoutParams belowBarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            belowBarLayoutParams.setMargins(0, Views.px(context, 30), 0, 0);
            belowBar.setLayoutParams(belowBarLayoutParams);
            view.addView(belowBar);

            RelativeLayout.LayoutParams showMapBtnLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            showMapBtnLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, showMapButton.getId());
            showMapBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, showMapButton.getId());
            belowBar.addView(showMapButton, showMapBtnLayoutParams);

            if (attribute.getDefinition().destinationPointSpecified) {
                navigateButton = createNavigationButton();
                RelativeLayout.LayoutParams navBtnLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                navBtnLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, navigateButton.getId());
                navBtnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, navigateButton.getId());
                navBtnLayoutParams.addRule(RelativeLayout.RIGHT_OF, showMapButton.getId());
                belowBar.addView(navigateButton, navBtnLayoutParams);
            }
        }

        private LinearLayout createSrsLayout() {
            TextView srsLabel = new TextView(context);
            srsLabel.setText(getString(R.string.label_spatial_reference_system) + ":");

            LinearLayout srsLine = new LinearLayout(context);
            srsLine.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            srsLine.setOrientation(LinearLayout.HORIZONTAL);
            srsLine.addView(srsLabel);
            srsLine.addView(srsSpinner);
            return srsLine;
        }

        private Spinner createSrsSpinner() {
            final Spinner srsSpinner = new AppCompatSpinner(context);
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
            Button button = new AppCompatButton(context);
            button.setTextAppearance(context, android.R.style.TextAppearance_Small);
            button.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setText(getString(R.string.label_navigate));
            button.setCompoundDrawablesWithIntrinsicBounds(
                    null, new Attrs(context).drawable(R.attr.navigationIcon), null, null);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    NavigationDialogFragment.show(context.getSupportFragmentManager());
                }
            });
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
            final TextView input = new AppCompatEditText(context);

            input.setSingleLine();
            if (value != null)
                input.setText(formatDouble(value));
            input.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint(hint);

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
                    if (! requestingLocation) {
                        input.setError(null);
                        delaySaveNode();
                    }
                }
            });
            return input;
        }


        private TextView createNumberOutput(Double value) {
            final TextView output = new AppCompatTextView(context);
            output.setSingleLine();
            if (value != null)
                output.setText(formatDouble(value));
            output.setInputType(TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_SIGNED | TYPE_NUMBER_FLAG_DECIMAL);
            return output;
        }

        private ToggleButton createStartStopButton() {
            ToggleButton button = new ToggleButton(context);
            button.setText(getString(R.string.label_start_gps));
            button.setTextOn(getString(R.string.label_stop_gps));
            button.setTextOff(getString(R.string.label_start_gps));
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
            return button;
        }

        private Button createShowMapButton() {
            Button button = new AppCompatButton(context);
            button.setText(getString(R.string.label_show_map));
            button.setGravity(Gravity.RIGHT);
            button.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            button.setCompoundDrawablesWithIntrinsicBounds(
                    null, new Attrs(context).drawable(R.attr.mapIcon), null, null);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!attribute.isEmpty()) {
                        double[] lonLat = transformToLonLat(attribute.getX(), attribute.getY());
                        double lon = lonLat[0];
                        double lat = lonLat[1];
                        String uri = String.format(Locale.ENGLISH,
                                "geo:%f,%f?z=17&q=%f,%f(%s)", lat, lon, lat, lon, attribute.getDefinition().label);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                }
            });
            button.setEnabled(!attribute.isEmpty());
            return button;
        }

        private TextView createAccuracyView() {
            return new TextView(context);
        }

        private void updateCoordinate(double[] transformedCoord) {
            xView.setText(formatDouble(transformedCoord[0]));
            yView.setText(formatDouble(transformedCoord[1]));
        }

        private String formatDouble(Double value) {
            return numberFormat().format(value);
        }

        private String getString(int resId) {
            return context.getResources().getString(resId);
        }
    }

    private class UpdateListener implements LocationProvider.LocationUpdateListener {
        private final FragmentActivity context;

        UpdateListener(FragmentActivity context) {this.context = context;}

        public void onUpdate(Location location) {
            float accuracy = location.getAccuracy();
            vh.accuracyView.setText(context.getResources().getString(R.string.label_accuracy) + ": " + Math.round(accuracy) + "m");
            double[] transformedCoord = transformToSelectedSrs(location);
            vh.updateCoordinate(transformedCoord);
        }
    }
}
