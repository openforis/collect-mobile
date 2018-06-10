package org.openforis.collect.android.gui.input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.openforis.collect.android.util.CollectPermissions;

public final class LocationProvider {
    private final LocationUpdateListener listener;
    private final Context context;
    private final boolean staticLocation;
    private final LocationManager locationManager;
    private final LocationUpdater locationUpdater = new LocationUpdater();

    public LocationProvider(LocationUpdateListener listener, Context context, boolean staticLocation) {
        this.listener = listener;
        this.context = context;
        this.staticLocation = staticLocation;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void start() {
        if (CollectPermissions.checkAccessLocationPermissionOrRequestIt((Activity) context)) {
            locationUpdater.bestAccuracy = Float.MAX_VALUE;
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            for (String provider : locationManager.getAllProviders()) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(provider, 1000, 0, locationUpdater, context.getMainLooper());
                }
            }
        }
    }

    public void stop() {
        locationManager.removeUpdates(locationUpdater);
    }

    private final class LocationUpdater implements LocationListener {
        private float bestAccuracy = Float.MAX_VALUE;

        public synchronized void onLocationChanged(Location location) {
            float accuracy = location.getAccuracy();
            if (!staticLocation || accuracy < bestAccuracy) {
                bestAccuracy = accuracy;
                listener.onUpdate(location);
            }
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

    public interface LocationUpdateListener {
        void onUpdate(Location location);
    }
}
