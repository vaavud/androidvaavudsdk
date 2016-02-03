package com.vaavud.vaavudSDK.core.location;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.LocationEventListener;
import com.vaavud.vaavudSDK.core.model.LatLng;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;

/**
 * Created by juan on 19/01/16.
 */
public class LocationService {
    private static final String TAG = "LocationService";

    private static final int LOCATION_REQUEST_PERMISIONS = 500;
    private static final long TWO_MINUTES = 1000L * 60L * 2L;
    private long locationDelay = 500;

    private final Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationEventListener locationEventListener;
    private Location lastLocation;
    private Geocoder geocoder;
    private boolean permisionGranted = false;


    public LocationService(Context _context, Long _locationDelay) {

        context = _context;
        if (_locationDelay != null)
            locationDelay = _locationDelay;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(context);

    }

    public void setLocationDelay(int _locationDelay) {
        locationDelay = _locationDelay;
    }

    public void start() throws VaavudError {

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (isBetterLocation(location, lastLocation)) {
                    Log.d(TAG, "Got better location (" + location.getLatitude() + "," + location.getLongitude() + ", " + location.getAccuracy() + ")");
                    //Log.i("LocationUpdateManager", "Got better location (" + location.getLatitude() + "," + location.getLongitude() + ", " + location.getAccuracy() + ")");
                    lastLocation = location;
                    locationEventListener.newVelocity(new VelocityEvent(lastLocation.getTime(), lastLocation.getSpeed()));
                    locationEventListener.newBearing(new BearingEvent(lastLocation.getTime(),lastLocation.getBearing()));
                }
                locationEventListener.newLocation(new LocationEvent(lastLocation.getTime(), new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Status Changed: " + provider + " " + status + " " + extras);
            }

            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Provider Enabled " + provider);
            }

            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Provider Disabled " + provider);
            }
        };

        for (String provider : locationManager.getAllProviders()) {
            locationManager.requestLocationUpdates(provider, locationDelay, 0, locationListener);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(provider);

            }
        }
    }

    public void stop() throws SecurityException {
        //Log.i("LocationUpdateManager", "removing location listener");
        locationManager.removeUpdates(locationListener);

    }

    public LatLng getLocation() {
        if (lastLocation != null && (System.currentTimeMillis() - lastLocation.getTime()) < TWO_MINUTES) {
            try {
                return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public Float getAltitude() {
        Float altitude = null;
        if (lastLocation != null && (System.currentTimeMillis() - lastLocation.getTime()) < TWO_MINUTES) {
            altitude = (float) lastLocation.getAltitude();
        }
        return altitude;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    private boolean isNewerLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > locationDelay;
        boolean isSignificantlyOlder = timeDelta < -locationDelay;


        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }
        return false;
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isNewer = timeDelta > 0;
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void setEventListener(LocationEventListener _locationEventListener) {
        locationEventListener = _locationEventListener;
    }


}
