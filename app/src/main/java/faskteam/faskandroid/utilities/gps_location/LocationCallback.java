package faskteam.faskandroid.utilities.gps_location;

import android.location.Location;

public interface LocationCallback {
    void onReceiveLocation(Location location);
}
