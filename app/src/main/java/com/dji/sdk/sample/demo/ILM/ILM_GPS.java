package com.dji.sdk.sample.demo.ILM;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class ILM_GPS {
    private double latitude = 0;
    private double longitude = 0;
    private double altitude = 0;
    private Context context;
    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final long MIN_UPDATE_INTERVAL_MS = 100; // Update every 100ms
    private static final float MIN_UPDATE_DISTANCE_M = 0;   // Update with every location change
    private Handler locationUpdateHandler = new Handler();

    public ILM_GPS(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    protected void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        try {
            // Request updates from both GPS and Network providers
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_INTERVAL_MS, MIN_UPDATE_DISTANCE_M, locationListener);
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_INTERVAL_MS, MIN_UPDATE_DISTANCE_M, locationListener);
            }
        } catch (Exception e) {
            Log.e("GPS ERROR", "Error requesting location updates", e);
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            //Log.d("GPS", "Location updated: Lat=" + location.getLatitude() + ", Lon=" + location.getLongitude());

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    };

    protected void onDestroy() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void updateLatitudeLongitudeAltitude(TextView latitudeView, TextView longitudeView, TextView altitudeView) {
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                latitudeView.setText(String.valueOf(latitude));
                longitudeView.setText(String.valueOf(longitude));
                altitudeView.setText(String.valueOf(altitude));
                locationUpdateHandler.postDelayed(this, 100);
            }
        };
        updateTimeRunnable.run();
    }

}
