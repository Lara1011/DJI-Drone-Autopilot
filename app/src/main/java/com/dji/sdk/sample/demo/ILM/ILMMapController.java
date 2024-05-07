package com.dji.sdk.sample.demo.ILM;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.ArrayList;
import java.util.Locale;

import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.flightcontroller.FlightController;

public class ILMMapController {
    private MapView mapView;
    private Context context;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;


    public ILMMapController(Context context, MapView mapView) {
        this.mapView = mapView;
        this.context = context;
    }

    protected void init(Context context, MapView mapView) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(18.0);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET
        });
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);

        CompassOverlay compassOverlay = new CompassOverlay(context, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        double[] points = new double[3];
        updatePinMark(points);
    }

    private void updatePinMark(double[] points) {
        Handler locationUpdateHandler = new Handler();
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        final Marker[] previousMarker = {null}; // Define previousMarker here

        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (flightController != null) {
                    LocationCoordinate3D aircraftLocation = flightController.getState().getAircraftLocation();
                    if (aircraftLocation != null) {
                        points[0] = aircraftLocation.getLatitude();
                        points[1] = aircraftLocation.getLongitude();
                        points[2] = aircraftLocation.getAltitude();
                        GeoPoint point = new GeoPoint(points[0], points[1], points[2]);

                        // Remove the previous marker if it exists
                        if (previousMarker[0] != null) {
                            mapView.getOverlays().remove(previousMarker[0]);
                        }

                        // Add a new marker
                        Marker startMarker = new Marker(mapView);
                        startMarker.setPosition(point);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                        mapView.getOverlays().add(startMarker);

                        // Set the current marker as the previous marker
                        previousMarker[0] = startMarker;

                        mapView.getController().setCenter(point);
                    }
                }
                locationUpdateHandler.postDelayed(this, 1000);
            }
        };
        updateTimeRunnable.run();
    }


    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}