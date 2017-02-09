package faskteam.faskandroid.utilities.gps_location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {

        private static final String CLASS_TAG = "MyLocationListener";

        private LocationCallback callback;

        public MyLocationListener(LocationCallback callback) {
            this.callback = callback;
        }

        static {
            Log.i(CLASS_TAG, "Searched GPS");
        }

        @Override
        public void onLocationChanged(Location location) {
            callback.onReceiveLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }