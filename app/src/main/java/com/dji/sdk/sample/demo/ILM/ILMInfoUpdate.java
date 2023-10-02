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

import dji.sdk.gimbal.Gimbal;

public class ILMInfoUpdate {
    private TextView Battery;
    private TextView x;
    private TextView y;
    private TextView z;
    private TextView latitude;
    private TextView longtitude;
    private TextView altitude;
    private TextView DateTime;
    private TextView Speed;
    private TextView Distance;
    private TextView Pitch;
    private TextView Roll;
    private TextView Yaw;
    private Handler handler = new Handler();


    public ILMInfoUpdate(TextView battery, TextView x, TextView y, TextView z, TextView latitude, TextView longtitude, TextView altitude, TextView dateTime,
                         TextView speed, TextView distance, TextView pitch, TextView roll, TextView yaw) {
        this.Battery = battery;
        this.x = x;
        this.y = y;
        this.z = z;
        this.latitude = latitude;
        this.longtitude = longtitude;
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


}