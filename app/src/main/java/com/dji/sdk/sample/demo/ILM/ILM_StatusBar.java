package com.dji.sdk.sample.demo.ILM;

import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.content.Context;

import com.dji.sdk.sample.R;

import android.app.Service;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.view.PresentableView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.gimbal.GimbalState;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;

public class ILM_StatusBar {
    private String battery, speed, x, y, z, pitch, roll, yaw, date, distance, latitude, longitude, altitude;
    private Handler dateUpdateHandler = new Handler();
    private Handler locationUpdateHandler = new Handler();

    public void updateDateTime(TextView date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault());
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                String formattedDateTime = dateFormat.format(new Date());
                if (date != null) {
                    date.setText(formattedDateTime);
                    setDate(date.getText().toString());
                }
                dateUpdateHandler.postDelayed(this, 1000);
            }
        };
        updateTimeRunnable.run();
    }

    public void updateBattery(TextView battery) {
        DJISampleApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
            @Override
            public void onUpdate(BatteryState djiBatteryState) {
                int batteryPercentage = djiBatteryState.getChargeRemainingInPercent();
                if (battery != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            battery.setText(String.valueOf(batteryPercentage) + "%");
                            setBattery(battery.getText().toString());
                        }
                    });
                }
            }
        });
    }

    public void updateSpeed(TextView speed) {
        final DecimalFormat decimalFormat = new DecimalFormat("0.0");

        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            DJISampleApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    if (flightControllerState != null) {
                        final float velocityX = flightControllerState.getVelocityX();
                        final float velocityY = flightControllerState.getVelocityY();
                        final float velocityZ = flightControllerState.getVelocityZ();
                        final String speedVal = decimalFormat.format(Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speed.setText(speedVal + "m/s");
                                setSpeed(speed.getText().toString());
                            }
                        });
                    }
                }
            });
        }
    }

    public void updateDistance(TextView distance, ILM_GPS gps) {
        final DecimalFormat decimalFormat = new DecimalFormat("0.0");
        gps.requestLocationUpdates();

        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            ILM_GPS finalGps = gps;
            DJISampleApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    if (flightControllerState != null) {
                        double lat1 = 0;
                        double lon1 = 0;
                        double alt1 = 0;
                        LocationCoordinate3D aircraftLocation = flightControllerState.getAircraftLocation();
                        if (aircraftLocation != null) {
                            lat1 = aircraftLocation.getLatitude();
                            lon1 = aircraftLocation.getLongitude();
                            alt1 = aircraftLocation.getAltitude();
                        }
                        double R = 6371.0 + (alt1 + finalGps.getAltitude()) / 2; // Earth radius in kilometers, taking average altitude
                        double lat1Rad = Math.toRadians(lat1);
                        double lon1Rad = Math.toRadians(lon1);
                        double lat2Rad = Math.toRadians(finalGps.getLatitude());
                        double lon2Rad = Math.toRadians(finalGps.getLongitude());
                        // Haversine formula
                        double dLat = lat2Rad - lat1Rad;
                        double dLon = lon2Rad - lon1Rad;
                        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dLon / 2), 2);
                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                        double temp = R * c;
                        final String distanceVal = decimalFormat.format(String.valueOf(temp));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                distance.setText(distanceVal + "m");
                                setDistance(distance.getText().toString());
                            }
                        });
                    }
                }
            });
        }
        gps.onDestroy();
        gps = null;
    }

    public void updateXYZ(TextView x, TextView y, TextView z) {
        final DecimalFormat decimalFormat = new DecimalFormat("0.00000");

        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            DJISampleApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    if (flightControllerState != null) {
                        final float velocityX = flightControllerState.getVelocityX();
                        final float velocityY = flightControllerState.getVelocityY();
                        final float velocityZ = flightControllerState.getVelocityZ();


                        // Format velocity values using the DecimalFormat
                        final String formattedVelocityX = decimalFormat.format(velocityX);
                        final String formattedVelocityY = decimalFormat.format(velocityY);
                        final String formattedVelocityZ = decimalFormat.format(velocityZ);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                x.setText(formattedVelocityX);
                                y.setText(formattedVelocityY);
                                z.setText(formattedVelocityZ);

                                setX(x.getText().toString());
                                setY(y.getText().toString());
                                setZ(z.getText().toString());
                            }
                        });
                    }
                }
            });
        }
    }

    public void updatePitchRollYaw(TextView pitch, TextView roll, TextView yaw) {
        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            Gimbal gimbal = DJISampleApplication.getProductInstance().getGimbal();
            if (gimbal != null) {
                gimbal.setStateCallback(new GimbalState.Callback() {
                    @Override
                    public void onUpdate(@NonNull GimbalState gimbalState) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float p = (float) gimbalState.getAttitudeInDegrees().getPitch();
                                float r = (float) gimbalState.getAttitudeInDegrees().getRoll();
                                float y = (float) gimbalState.getAttitudeInDegrees().getYaw();

                                pitch.setText(Float.toString(p));
                                roll.setText(Float.toString(r));
                                yaw.setText(Float.toString(y));

                                setPitch(pitch.getText().toString());
                                setRoll(roll.getText().toString());
                                setYaw(yaw.getText().toString());
                            }
                        });
                    }
                });
            }
        }
    }

    public void updateLatitudeLongitudeAltitude(TextView latitude, TextView longitude, TextView altitude) {
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

                        setLatitude(latitude.getText().toString());
                        setLongitude(longitude.getText().toString());
                        setAltitude(altitude.getText().toString());
                    }
                }
                locationUpdateHandler.postDelayed(this, 100);
            }
        };
        updateTimeRunnable.run();
    }

    public String getBattery() {
        return battery;
    }

    public String getILMX() {
        return x;
    }

    public String getILMY() {
        return y;
    }

    public String getILMZ() {
        return z;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public String getDate() {
        return date;
    }

    public String getSpeed() {
        return speed;
    }

    public String getDistance() {
        return distance;
    }

    public String getPitch() {
        return pitch;
    }

    public String getRoll() {
        return roll;
    }

    public String getYaw() {
        return yaw;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setX(String x) {
        this.x = x;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public void setYaw(String yaw) {
        this.yaw = yaw;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }
}