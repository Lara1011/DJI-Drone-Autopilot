package com.dji.sdk.sample.demo.ILM;

import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.view.PresentableView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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



    private static final String TAG = "ILMStatusBar";
    private static final String CSV_HEADER = "Date,Distance,Battery,Speed,X,Y,Z,Latitude,Longitude,Altitude,Roll,Pitch,Yaw";
    private static final String CSV_FILENAME = "sdcard/droneLog/ILM_Log_File_aircraft_data.csv";

    private Handler csvUpdateHandler = new Handler();
    private File csvFile;
    private FileWriter csvWriter;
    private static final int CSV_UPDATE_INTERVAL_MS = 100; // Update every 100 milliseconds

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
        //createCsvFile();
        //startCsvUpdateTask();
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


    private void createCsvFile() {
        try {
            // Check if external storage is available
            if (isExternalStorageWritable()) {
                File dir = new File(Environment.getExternalStorageDirectory(), "AircraftData");
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.e(TAG, "Failed to create directory for CSV file");
                        return;
                    }
                }
                csvFile = new File(dir, CSV_FILENAME);
                csvWriter = new FileWriter(csvFile,true);
                if (csvFile.length() == 0) {
                    csvWriter.append(CSV_HEADER);
                    csvWriter.append("\n");
                }
                csvWriter.flush();
            } else {
                Log.e(TAG, "External storage is not writable");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating CSV file: " + e.getMessage());
        }
    }





    public void uploadToCsv() {
        String data = getDate() + "," + getDistance() + "," + getBattery() + "," + getSpeed() + ","
                + getILMX() + "," + getILMY() + "," + getILMZ() + "," + getLatitude() + ","
                + getLongitude() + "," + getAltitude() + "," + getRoll() + "," + getPitch() + ","
                + getYaw();


        // Write the data to the CSV file
        if (csvWriter != null) {
            try {
                csvWriter.append(data);
                csvWriter.append("\n");
                csvWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to CSV file: " + e.getMessage());
            }
        }
    }


    private void startCsvUpdateTask() {
        csvUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadToCsv(); // Call the function to update the CSV file
                csvUpdateHandler.postDelayed(this, CSV_UPDATE_INTERVAL_MS);
            }
        }, CSV_UPDATE_INTERVAL_MS);
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

    public void closeWriter(){
        if (csvWriter != null) {
            try {
                csvWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing CSV writer: " + e.getMessage());
            }
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
