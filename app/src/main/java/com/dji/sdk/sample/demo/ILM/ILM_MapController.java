package com.dji.sdk.sample.demo.ILM;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.flightcontroller.FlightController;

public class ILM_MapController {
    private MapView mapView;
    private Context context;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Handler locationUpdateHandler;
    private Runnable updateTimeRunnable;
    private Marker previousMarker = null;
    private List<Marker> waypointMarkers = new ArrayList<>();

    private Drawable arrowIcon;
    private Drawable pinIcon;
    private Drawable destinationIcon;
    protected boolean isMapCentered = false;

    public ILM_MapController(Context context, MapView mapView) {
        this.mapView = mapView;
        this.context = context;
        initIcons(context);
    }

    private void initIcons(Context context) {
        arrowIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ilm_arrow, null);
        pinIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ilm_red_pin, null);
        destinationIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ilm_green_pin, null);
    }

    private Drawable resizeDrawable(Drawable image, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        image.draw(canvas);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    protected void init() {
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
        locationUpdateHandler = new Handler();
        FlightController flightController = ModuleVerificationUtil.getFlightController();

        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (flightController != null) {
                    FlightControllerState state = flightController.getState();
                    LocationCoordinate3D aircraftLocation = state.getAircraftLocation();
                    if (aircraftLocation != null) {
                        points[0] = aircraftLocation.getLatitude();
                        points[1] = aircraftLocation.getLongitude();
                        points[2] = aircraftLocation.getAltitude();
                        GeoPoint point = new GeoPoint(points[0], points[1], points[2]);

                        if (previousMarker != null) {
                            mapView.getOverlays().remove(previousMarker);
                        }

                        Marker startMarker = new Marker(mapView);
                        startMarker.setPosition(point);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

                        if (state.isFlying()) {
                            float heading = (float) state.getAttitude().yaw; // Get the yaw angle in degrees
                            Drawable rotatedArrowIcon = rotateDrawable(arrowIcon, heading); // Rotate arrow icon
                            startMarker.setIcon(resizeDrawable(rotatedArrowIcon, 50, 50)); // Resize and set the rotated arrow icon
                        } else {
                            startMarker.setIcon(resizeDrawable(arrowIcon, 50, 50)); // Resize and set the arrow icon
                        }
                        mapView.getOverlays().add(startMarker);
                        previousMarker = startMarker;
                        if (!isMapCentered) {
                            mapView.getController().setCenter(point);
                            isMapCentered = true;
                        }
                    }
                }
                locationUpdateHandler.postDelayed(this, 1000);
            }
        };
        locationUpdateHandler.post(updateTimeRunnable);
    }


    private Drawable rotateDrawable(Drawable drawable, float angleDegrees) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(angleDegrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return new BitmapDrawable(context.getResources(), rotatedBitmap);
    }


    public void stopLocationUpdates() {
        if (locationUpdateHandler != null && updateTimeRunnable != null) {
            locationUpdateHandler.removeCallbacks(updateTimeRunnable);
        }
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

    public void addWaypoint(String latitude, String longitude, String altitude) {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        LocationCoordinate3D aircraftLocation = flightController.getState().getAircraftLocation();
        if (aircraftLocation != null) {
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            double alt = Double.parseDouble(altitude);
            GeoPoint waypoint = new GeoPoint(lat, lon, alt);

            Marker waypointMarker = new Marker(mapView);
            waypointMarker.setPosition(waypoint);
            waypointMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            waypointMarker.setIcon(resizeDrawable(pinIcon, 50, 50));

            mapView.getOverlays().add(waypointMarker);
            waypointMarkers.add(waypointMarker);
            mapView.getController().setCenter(waypoint);
            waypointMarker.setSnippet("Latitude: " + latitude + "\nLongitude: " + longitude + "\nAltitude: " + altitude);
            waypointMarker.setTextLabelFontSize(12);
//            waypointMarker.setTextIcon("Waypoint " + (waypointMarkers.size() - 1));
        }
    }

    public void removeWaypoint(String latitude, String longitude, String altitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        double alt = Double.parseDouble(altitude);

        Marker markerToRemove = null;
        Log.e("Markers size", String.valueOf(waypointMarkers.size()));

        // Use an iterator to safely remove items while iterating
        Iterator<Marker> iterator = waypointMarkers.iterator();
        while (iterator.hasNext()) {
            Marker marker = iterator.next();
            GeoPoint markerPosition = marker.getPosition();
            Log.e("Marker", "Marker Lat: " + markerPosition.getLatitude() + ", Lon: " + markerPosition.getLongitude() + ", Alt: " + markerPosition.getAltitude());
            Log.e("Waypoint to be removed", "Lat: " + lat + ", Lon: " + lon + ", Alt: " + alt);

            if (markerPosition.getLatitude() == lat && markerPosition.getLongitude() == lon && markerPosition.getAltitude() == alt) {
                markerToRemove = marker;
                while (mapView.getOverlays().contains(markerToRemove)) {
                    mapView.getOverlays().remove(markerToRemove);
                }
                iterator.remove(); // Safely remove the marker from the list
                Log.e("removeWaypoint", "waypoint removed");
            }
        }

        if (markerToRemove != null) {
            mapView.getOverlays().remove(markerToRemove);
        }

        mapView.invalidate();
    }


    public void hideAllWaypoints() {
        for (Marker marker : waypointMarkers) {
            mapView.getOverlays().remove(marker);
        }
        mapView.invalidate();
    }

    public void showDestinationPin(double latitude, double longitude, double altitude) {
        Marker destinationMarker = null;
        for (Marker marker : waypointMarkers) {
            GeoPoint markerPosition = marker.getPosition();
            if (markerPosition.getLatitude() == latitude && markerPosition.getLongitude() == longitude && markerPosition.getAltitude() == altitude) {
                destinationMarker = marker;
                break;
            }
        }
        if (destinationMarker != null) {
            //destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            destinationMarker.setIcon(resizeDrawable(destinationIcon, 50, 50));
            mapView.getOverlays().add(destinationMarker);
        }
    }

    public void showAllWaypoints() {
        hideAllWaypoints();
        for (Marker marker : waypointMarkers) {
            marker.setIcon(resizeDrawable(pinIcon, 50, 50));
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }
}