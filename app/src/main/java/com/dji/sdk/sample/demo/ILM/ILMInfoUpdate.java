package com.dji.sdk.sample.demo.ILM;

import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
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
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flighthub.model.FlightPathNode;
import dji.sdk.gimbal.Gimbal;

public class ILMInfoUpdate implements PresentableView {
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
    private FlightPathNode flightPathNode = new FlightPathNode();
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

    public String getBattery() {
        return Battery.getText().toString();
    }


    public String getILMX() {
        return x.getText().toString();
    }


    public String getILMY() {
        return y.getText().toString();
    }


    public String getILMZ() {
        return z.getText().toString();
    }


    public String getLatitude() {
        return (latitude.getText()).subSequence(10,latitude.getText().length()).toString();
    }


    public String getLongitude() {
        return (longitude.getText()).subSequence(11,longitude.getText().length()).toString();
    }


    public String getAltitude() {
        return (altitude.getText()).subSequence(10,altitude.getText().length()).toString();
    }


    public String getDate() {
        return DateTime.getText().toString();
    }


    public String getSpeed() {
        return Speed.getText().toString();
    }


    public String getDistance() {
        return Distance.getText().toString();
    }


    public String getPitch() {
        return Pitch.getText().toString();
    }


    public String getRoll() {
        return Roll.getText().toString();
    }


    public String getYaw() {
        return Yaw.getText().toString();
    }


    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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

                        latitude.setText("Latitude: "+String.format(Locale.getDefault(), "%.6f", lat));
                        longitude.setText("Longitude: "+String.format(Locale.getDefault(), "%.6f", lon));
                        altitude.setText("Altitude: "+String.format(Locale.getDefault(), "%.6f", alt));
                    }
                }
                locationUpdateHandler.postDelayed(this, 100);
            }
        };
        updateTimeRunnable.run();
    }

    public void updateXYZ() {
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
                            }
                        });
                    }
                }
            });
        }
    }

    public void updateSpeed() {
        final DecimalFormat decimalFormat = new DecimalFormat("0.0");

        if (ModuleVerificationUtil.isFlightControllerAvailable()) {
            DJISampleApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    if (flightControllerState != null) {
                        final float velocityX = flightControllerState.getVelocityX();
                        final float velocityY = flightControllerState.getVelocityY();
                        final float velocityZ = flightControllerState.getVelocityZ();
                        final String speed = decimalFormat.format(Math.sqrt(velocityX*velocityX + velocityY*velocityY + velocityZ*velocityZ));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Speed.setText(speed+"m/s");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public int getDescription() {
        return 0;
    }

    @NonNull
    @Override
    public String getHint() {
        return null;
    }
}
