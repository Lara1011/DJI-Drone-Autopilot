package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.util.Log;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

public class ILM_GoTo implements ILM_iGoTo {
    private int mode = 0;
    private double lat = 0;
    private double lon = 0;
    private double alt = 0;
    private double speed = 2; //ToDo: change it to 2
    private double radius = 10;
    private double minDistance = 2;
    private FlightController flightController = null;
    protected boolean isGoTo = true;
    private float yaw;
    private float pitch;
    private float roll;
    protected boolean isRepeatRoute = false;
    private int count = 0;
    private ILM_Waypoints ilmWaypoints;
    private ILM_MapController mapController;

    public ILM_GoTo(ILM_Waypoints ilmWaypoints, ILM_MapController mapController) {
        this.ilmWaypoints = ilmWaypoints;
        this.mapController = mapController;
        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            flightController = DJISampleApplication.getAircraftInstance().getFlightController();
        }
    }

    public ILM_GoTo(double lat, double lon, double alt, double speed, double radius, double minDistance) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.speed = speed;
        this.radius = radius;
        this.minDistance = minDistance;
        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            flightController = DJISampleApplication.getAircraftInstance().getFlightController();
        }
    }

    @Override
    public void goTo() {
        if (lat == 0 && lon == 0 && alt == 0) {
            showToast("Please set waypoint first!");
            return;
        }
        if (!isGoTo)
            return;
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Virtual sticks enabled!");
                } else showToast("Failed to enable virtual sticks: " + djiError);
            }
        });
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        if (mapController != null) {
            mapController.showDestinationPin(lat, lon, alt);
        }
        Log.e("--------mode--------", String.valueOf(this.mode));
        if (this.mode == 1) {
            Log.e("--------mode 1--------", String.valueOf(this.mode));
            goToMode1();
            Log.e("--------end of mode 1--------", String.valueOf(this.mode));

        } else if (this.mode == 2) {
            Log.e("--------mode 2--------", String.valueOf(this.mode));
            goToMode2();
        }
    }

    private void goToMode1() {
        double angle = calculateBearing();
        if (angle > 180)
            yaw = (float) (-360 + angle);
        if (angle < -180)
            yaw = (float) (360 + angle);
        if (angle < 180 && angle > -180)
            yaw = (float) angle;
        Timer forward_timer = new Timer();

        final double[] distance = {Integer.MAX_VALUE};
        distance[0] = distance();
        final double[] finalDistance = {distance[0]};
        forward_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (finalDistance[0] > minDistance * 0.001 && isGoTo && finalDistance[0] < radius && alt > 3.0) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(0, (float) speed, yaw, (float) alt),
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e("TAG", "Couldn't fly towards waypoint: " + djiError.getDescription());
                                    } else {
                                        Log.d("TAG", "Flying Towards Waypoint!");
                                    }
                                }
                            });
                    double angle = calculateBearing();

                    if (angle > 180)
                        yaw = (float) (-360 + angle);
                    if (angle < -180)
                        yaw = (float) (360 + angle);
                    if (angle < 180 && angle > -180)
                        yaw = (float) angle;

                    finalDistance[0] = distance();
                    Log.e("Distance", String.valueOf(finalDistance[0]));
                    Log.e("yaw", String.valueOf(angle));
                } else {
                    if (mapController != null)
                        mapController.showAllWaypoints();
                    forward_timer.cancel();
                    flightController.setVirtualStickModeEnabled(false, null);
                    if (isRepeatRoute) {
                        HashMap waypoints = ilmWaypoints.getWaypoints();
                        if (count == (waypoints.size() / 4) - 1) {
                            isRepeatRoute = false;
                            count = 0;
                            double lat = Double.parseDouble((String) waypoints.get("Latitude" + count));
                            double lon = Double.parseDouble((String) waypoints.get("Longitude" + count));
                            double alt = Double.parseDouble((String) waypoints.get("Altitude" + count));
                            setWaypoint(lat, lon, alt);
                            goTo();
                        } else {
                            count++;
                            double lat = Double.parseDouble((String) waypoints.get("Latitude" + count));
                            double lon = Double.parseDouble((String) waypoints.get("Longitude" + count));
                            double alt = Double.parseDouble((String) waypoints.get("Altitude" + count));
                            setWaypoint(lat, lon, alt);
                            goTo();
                        }
                    } else {
                        forward_timer.cancel();
                        flightController.setVirtualStickModeEnabled(false, null);
                        isGoTo = true;
                    }
                }

            }
        }, 0, 350);
    }

    private void goToMode2() {
        yaw = (float) flightController.getState().getAttitude().yaw;
        double angle = calculateBearing();
        pitch = (float) (Math.sin(Math.toRadians(angle)) * 2);
        roll = (float) (Math.cos(Math.toRadians(angle)) * 2);

        final double[] distance = {Integer.MAX_VALUE};
        distance[0] = distance();
        final double[] finalDistance = {distance[0]};
        Timer forward_timer = new Timer();
        forward_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (finalDistance[0] > 0.001 && isGoTo && finalDistance[0] < radius && alt > 3.0) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(pitch, roll, yaw, (float) alt),
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e("TAG", "Couldn't fly towards waypoint: " + djiError.getDescription());
                                    } else {
                                        Log.d("TAG", "Flying Towards Waypoint!");
                                    }
                                }
                            });

                    double angle = calculateBearing();
                    pitch = (float) (Math.sin(Math.toRadians(angle)) * 2);
                    roll = (float) (Math.cos(Math.toRadians(angle)) * 2);
                    finalDistance[0] = distance();
                    Log.e("Roll", String.valueOf(roll));
                    Log.e("Pitch", String.valueOf(pitch));
                    Log.e("Distance", String.valueOf(finalDistance[0]));
                } else {
                    forward_timer.cancel();
                    flightController.setVirtualStickModeEnabled(false, null);
                    isGoTo = true;
                }

            }
        }, 0, 350);
    }

    private double calculatePitch() {
        double currentLat = flightController.getState().getAircraftLocation().getLatitude();
        double currentLon = flightController.getState().getAircraftLocation().getLongitude();
        double currentAlt = flightController.getState().getAircraftLocation().getAltitude();
        double latDiff = this.lat - currentLat; // Difference in latitude
        double lonDiff = this.lon - currentLon; // Difference in longitude
        double altDiff = this.alt - currentAlt; // Difference in altitude

        double distanceXY = Math.sqrt(latDiff * latDiff + lonDiff * lonDiff); // Distance in the XY plane
        Log.e("Pitch", String.valueOf((Math.toDegrees(Math.atan2(altDiff, distanceXY)) + 360) % 360));
        return (Math.toDegrees(Math.atan2(altDiff, distanceXY)) + 360) % 360;
    }

    private double calculateRoll() {
        double currentLat = flightController.getState().getAircraftLocation().getLatitude();
        double currentLon = flightController.getState().getAircraftLocation().getLongitude();
        double latDiff = this.lat - currentLat; // Difference in latitude
        double lonDiff = this.lon - currentLon; // Difference in longitude
        Log.e("Roll", String.valueOf((Math.toDegrees(Math.atan2(lonDiff, latDiff)) + 360) % 360));
        return (Math.toDegrees(Math.atan2(-lonDiff, latDiff)) + 360) % 360;
    }

    @Override
    public double distance() {
        double lat1 = 0;
        double lon1 = 0;
        double alt1 = 0;
        LocationCoordinate3D aircraftLocation = flightController.getState().getAircraftLocation();
        if (aircraftLocation != null) {
            lat1 = aircraftLocation.getLatitude();
            lon1 = aircraftLocation.getLongitude();
            alt1 = aircraftLocation.getAltitude();
        }
        double R = 6371.0 + (alt1 + this.alt) / 2; // Earth radius in kilometers, taking average altitude
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(this.lat);
        double lon2Rad = Math.toRadians(this.lon);
        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }

    @Override
    public int getMode() {
        return this.mode;
    }

    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void setWaypoint(Double lat, Double lon, Double alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    @Override
    public double getLat() {
        return this.lat;
    }

    @Override
    public double getLon() {
        return this.lon;
    }

    @Override
    public double getAlt() {
        return this.alt;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    private double calculateBearing() {
        FlightController flightController = DJISampleApplication.getAircraftInstance().getFlightController();
        double lat1 = Double.parseDouble(String.valueOf(flightController.getState().getAircraftLocation().getLatitude()));
        double lon1 = Double.parseDouble(String.valueOf(flightController.getState().getAircraftLocation().getLongitude()));
        double deltaLon = Math.toRadians(this.lon - lon1);
        lat1 = Math.toRadians(lat1);
        double lat2 = Math.toRadians(this.lat);
        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);
        // Convert radians to degrees
        bearing = Math.toDegrees(bearing);
        // Normalize bearing to range [0, 360)
        bearing = (bearing + 360) % 360;
        return bearing;
    }

    public void setIsGoTo(boolean isGoTo) {
        this.isGoTo = isGoTo;
    }

    public void disableGoTo() {
        if (flightController != null) {
            flightController.setVirtualStickModeEnabled(false, null);
        }
        this.isGoTo = false;
    }

    public void resetCount() {
        this.count = 0;
    }
}
