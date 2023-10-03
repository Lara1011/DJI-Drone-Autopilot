package com.dji.sdk.sample.demo.ILM;

import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.os.Handler;
import android.widget.TextView;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;

public class ILMInfoUpdate {
    private TextView Battery;
    private TextView x;
    private TextView y;
    private TextView z;
    private TextView latitude;
    private TextView longitude;
    private TextView altitude;
    private TextView DateTime;
    private TextView Speed;
    private TextView Distance;
    private TextView Pitch;
    private TextView Roll;
    private TextView Yaw;
    private Handler handler = new Handler();
    private Handler locationUpdateHandler = new Handler();
    private Handler xyzUpdateHandler = new Handler();

    public ILMInfoUpdate(TextView battery, TextView x, TextView y, TextView z, TextView latitude, TextView longtitude, TextView altitude, TextView dateTime,
                         TextView speed, TextView distance, TextView pitch, TextView roll, TextView yaw) {
        this.Battery = battery;
        this.x = x;
        this.y = y;
        this.z = z;
        this.latitude = latitude;
        this.longitude = longtitude;
        this.altitude = altitude;
        this.DateTime = dateTime;
        this.Speed = speed;
        this.Distance = distance;
        this.Pitch = pitch;
        this.Roll = roll;
        this.Yaw = yaw;
    }

    public void updateGimbalState() {
        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            Gimbal gimbal = DJISampleApplication.getProductInstance().getGimbal();
            if (gimbal != null) {
                gimbal.setStateCallback(gimbalState -> {
                    runOnUiThread(() -> {
                        Pitch.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getPitch()));
                        Roll.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getRoll()));
                        Yaw.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getYaw()));
                    });
                });
            }
        }
    }


    public void updateDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault());
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                String formattedDateTime = dateFormat.format(new Date());

                if (DateTime != null) {
                    DateTime.setText(formattedDateTime);
                }
                handler.postDelayed(this, 1000);
            }
        };
        updateTimeRunnable.run();
    }

    public void updateBatteryPercentage() {
        DJISampleApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
            @Override
            public void onUpdate(BatteryState djiBatteryState) {
                int batteryPercentage = djiBatteryState.getChargeRemainingInPercent();
                if (Battery != null) {
                    runOnUiThread(() -> {
                        Battery.setText(String.valueOf(batteryPercentage) + "%");
                    });
                }
            }
        });
    }

    public void updateLatitudeLongitude() {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (flightController != null) {
                    LocationCoordinate3D aircraftLocation = flightController.getState().getAircraftLocation();
                    if (aircraftLocation != null) {
                        double lat = aircraftLocation.getLatitude();
                        double lon = aircraftLocation.getLongitude();
                        double alt = aircraftLocation.getAltitude();

                        latitude.setText(String.format(Locale.getDefault(), "%.6f", lat));
                        longitude.setText(String.format(Locale.getDefault(), "%.6f", lon));
                        altitude.setText(String.format(Locale.getDefault(), "%.6f", alt));
                    }
                }
                locationUpdateHandler.postDelayed(this, 1000);
            }
        };
        updateTimeRunnable.run();
    }

    public void updateXYZ() {
        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            DJISampleApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    if (flightControllerState != null) {
                        final int velocityX = (int) flightControllerState.getVelocityX();
                        final int velocityY = (int) flightControllerState.getVelocityY();
                        final int velocityZ = (int) flightControllerState.getVelocityZ();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                x.setText(String.format(Locale.getDefault(), "%d", velocityX));
                                y.setText(String.format(Locale.getDefault(), "%d", velocityY));
                                z.setText(String.format(Locale.getDefault(), "%d", velocityZ));
                                xyzUpdateHandler.postDelayed(this, 1000);
                            }
                        });
                    }
                }
            });
        }
    }
}