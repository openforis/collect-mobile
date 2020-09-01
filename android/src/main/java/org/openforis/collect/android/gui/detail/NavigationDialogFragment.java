package org.openforis.collect.android.gui.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.openforis.collect.R;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.gui.ServiceLocator;
import org.openforis.collect.android.gui.input.BearingToNorthProvider;
import org.openforis.collect.android.gui.input.LocationProvider;
import org.openforis.collect.android.gui.util.Keyboard;
import org.openforis.collect.android.util.CoordinateTranslator;
import org.openforis.collect.android.util.CoordinateUtils;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

import static org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem.LAT_LNG_SRS;

public class NavigationDialogFragment extends DialogFragment {

    private LocationProvider locationProvider;
    private ViewHolder vh;
    private UiCoordinateAttribute attribute;
    private BearingToNorthProvider compassBearingProvider;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        UiNode selectedNode = ServiceLocator.surveyService().selectedNode();
        final UpdateListener updateListener = new UpdateListener();
        if (selectedNode instanceof UiCoordinateAttribute) {
            locationProvider = new LocationProvider(updateListener, getActivity(), false);
        } else
            throw new IllegalStateException("Opening code list description for invalid node: " + selectedNode);

        this.attribute = (UiCoordinateAttribute) selectedNode;
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        compassBearingProvider = new BearingToNorthProvider(getActivity());
        compassBearingProvider.setChangeEventListener(new BearingToNorthProvider.ChangeEventListener() {
            public void onBearingChanged(double bearing) {
                updateListener.onUpdate(updateListener.lastLocation);
            }
        });
        return dialog;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vh = new ViewHolder(inflater, container);
        return vh.view;
    }

    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = getDialog().getWindow();
            Point size = new Point();
            Display display = window.getWindowManager().getDefaultDisplay();
            display.getSize(size);

            int side = (int) Math.min(size.x, size.y * 0.75);
            int paddedSide = (int) (side * 0.90);
            window.setLayout(paddedSide, LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        if (compassBearingProvider.hasMagneticFieldSensor() && Settings.isCompassEnabled())
            compassBearingProvider.start();
        getView().post(new Runnable() {
            public void run() {
                if (getActivity() == null)
                    return;
                View navigationCircle = vh.navigationCircle;
                int navigationCircleDiameter = navigationCircle.getWidth();
                navigationCircle.getLayoutParams().height = navigationCircleDiameter; // Force the height to be set

                int destinationRadius = dpToPx(5);
                int radius = (int) (navigationCircleDiameter / 2d - destinationRadius);
                placeNavigationLabel(R.id.navigationN, radius, destinationRadius);
                placeNavigationLabel(R.id.navigationE, radius * 2 - destinationRadius, radius);
                placeNavigationLabel(R.id.navigationS, radius, radius * 2 - 2 * destinationRadius);
                placeNavigationLabel(R.id.navigationW, destinationRadius, radius);
            }
        });
        locationProvider.start();

    }

    private void placeNavigationLabel(int id, int x, int y) {
        View label = vh.navigationCircle.findViewById(id);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) label.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        label.setVisibility(View.VISIBLE);
    }

    public void onPause() {
        super.onPause();
        locationProvider.stop();
        if (compassBearingProvider.hasMagneticFieldSensor())
            compassBearingProvider.stop();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        Keyboard.hide(getActivity());
        super.onViewCreated(view, savedInstanceState);
    }

    public static void show(FragmentManager fragmentManager) {
        new NavigationDialogFragment().show(fragmentManager, "navigation");
    }

    private class ViewHolder {
        final ViewGroup view;
        final View navigationCircle;
        final View destinationCircle;
        final TableLayout navigationData;
        final TextView distance;
        final TextView accuracy;
        final TextView destinationBearing;

        ViewHolder(LayoutInflater inflater, ViewGroup container) {
            view = (ViewGroup) inflater.inflate(R.layout.navigation, container, false);
            navigationCircle = view.findViewById(R.id.navigationCircle);
            destinationCircle = navigationCircle.findViewById((R.id.destinationCircle));
            navigationData = (TableLayout) view.findViewById(R.id.navigationData);
            distance = (TextView) view.findViewById(R.id.navigationDestinationDistance);
            accuracy = (TextView) view.findViewById(R.id.navigationAccuracy);
            destinationBearing = (TextView) view.findViewById(R.id.navigationDestinationBearing);
        }

        void refresh(double compassBearing, double bearing, double distance, double accuracy, ValidationResultFlag validationResultFlag) {
            View navigationCircle = vh.navigationCircle;
            int circleWidth = navigationCircle.getWidth();

            int destinationRadius = dpToPx(10);
            double radius = circleWidth / 2d - destinationRadius;
            int[] margins = CoordinateTranslator.toMargins(0, bearing, radius);

            View destinationCircle = vh.destinationCircle;
            RelativeLayout.LayoutParams destinationParams = (RelativeLayout.LayoutParams) destinationCircle.getLayoutParams();
            destinationParams.leftMargin = margins[0];
            destinationParams.topMargin = margins[1];
            destinationCircle.setVisibility(View.VISIBLE);
            GradientDrawable background = (GradientDrawable) destinationCircle.getBackground();
            int color = color(distance, accuracy, validationResultFlag);
            background.setStroke(5, color);
            background.setColor(color);

            vh.distance.setText(String.valueOf(Math.round(distance)) + "m");
            vh.distance.setTextColor(color);
            vh.accuracy.setText(String.valueOf(Math.round(accuracy)) + "m");
            vh.destinationBearing.setText(String.valueOf(Math.round(bearing)) + "°");

            navigationCircle.setRotation((float) -compassBearing);

            view.requestLayout();
            navigationCircle.requestLayout();
        }

        int color(double distance, double accuracy, ValidationResultFlag validationResultFlag) {
            if (validationResultFlag == ValidationResultFlag.ERROR)
                return Color.parseColor("#dc143c");
            if (validationResultFlag == ValidationResultFlag.WARNING)
                return Color.parseColor("#ff8c00");
            if (distance > accuracy)
                return Color.parseColor("#ffd700");
            return Color.parseColor("#32cd32");
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private class UpdateListener implements LocationProvider.LocationUpdateListener {
        Location lastLocation;
        double lastCompassBearing;

        public void onUpdate(Location location) {
            if (location == null || getActivity() == null)
                return;
            double compassBearing = getCompassBearing();
            if (lastCompassBearing != compassBearing || !sameLocation(location, lastLocation)) {
                lastLocation = location;
                lastCompassBearing = compassBearing;
                double[] current = new double[]{location.getLongitude(), location.getLatitude()};
                double[] destination = ServiceLocator.coordinateDestinationService().destination(attribute, current);
                if (destination == null)
                    return;
                ValidationResultFlag validationResultFlag = ServiceLocator.coordinateDestinationService().validateDistance(attribute, current);
                double bearing = CoordinateUtils.bearing(LAT_LNG_SRS, current, LAT_LNG_SRS, destination);
                double distance = CoordinateUtils.distance(LAT_LNG_SRS, current, LAT_LNG_SRS, destination);
                double accuracy = location.getAccuracy();


                vh.refresh(compassBearing, bearing, distance, accuracy, validationResultFlag);
            }
        }

        private double getCompassBearing() {
            double bearing = compassBearingProvider.getBearing();
            if (Double.isNaN(bearing))
                return 0;
            if (bearing < 0) return 360 + bearing;
            return bearing;
        }


        @SuppressWarnings("SimplifiableIfStatement")
        boolean sameLocation(Location l1, Location l2) {
            if (l1 == l2)
                return true;
            if (l1 == null || l2 == null)
                return false;
            return (l1.getLatitude() == l2.getLatitude()
                    && l1.getLongitude() == l2.getLongitude()
                    && l1.getAccuracy() == l2.getAccuracy());
        }
    }
}
