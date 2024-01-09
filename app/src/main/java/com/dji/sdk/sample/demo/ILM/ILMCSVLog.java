
//        logFileName = "sdcard/droneLog/ILM_Log_File_" + System.currentTimeMillis() + ".csv";
//        header = new String[]{"TimeMS", "date", "time", "Lat", "Lon", "Alt", "HeadDirection", "VelocityX", "VelocityY", "VelocityZ", "yaw", "pitch", "roll", "Throttle", "UsAlt", "GimbalPitch",
//                "batRemainingTime", "batCharge", "MarkerX", "MarkerY", "MarkerZ", "PitchOutput", "RollOutput", "ErrorX", "ErrorY", "Pp", "Ip", "Dp", "Pr", "Ir", "Dr", "Pt", "It", "Dt", "MaxI", "Mode"};

package com.dji.sdk.sample.demo.ILM;

import android.content.Context;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class ILMCSVLog {
    private Context context;
    private FileWriter writer;
    private Timer counterTimer;
    private ILMInfoUpdate infoUpdate;


    public ILMCSVLog(Context context, ILMInfoUpdate infoUpdate) {
        this.context = context;
        this.infoUpdate = infoUpdate;
    }

    private void createCSVFile() {
        String filename = "ILM_DJI_Drone_Data-" + infoUpdate.getDate() + ".csv";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        boolean isFileExists = file.exists();
        try {
            writer = new FileWriter(file, true); // Append mode
            if (!isFileExists) {
                writer.append("Date,Time,Battery,Speed,Distance,X,Y,Z,Pitch,Roll,Yaw,Latitude,Longitude,Altitude,Mode").append('\n'); // Header row
            }
            Toast.makeText(context, "CSV file created at " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeCSVFile() {
        if (writer != null) {
            try {
                writer.close();
                writer = null; // Reset the writer
                Toast.makeText(context, "CSV file closed.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "No CSV file to close.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startUpdatingCounter() {
        // Start the timer to update the counter
        counterTimer = new Timer();
        counterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (writer != null) {
                    updateCSVInfo();
                }
            }
        }, 0, 100); // Update every 100ms
    }

    private void updateCSVInfo() {
        if (writer != null) {
            try {
                writer.append(infoUpdate.getDate()).append(",").
                        append(infoUpdate.getBattery()).append(",").
                        append(infoUpdate.getSpeed()).append(",").
                        append(infoUpdate.getDistance()).append(",").
                        append(infoUpdate.getILMX()).append(",").
                        append(infoUpdate.getILMY()).append(",").
                        append(infoUpdate.getILMZ()).append(",").
                        append(infoUpdate.getPitch()).append(",").
                        append(infoUpdate.getRoll()).append(",").
                        append(infoUpdate.getYaw()).append(",").
                        append(infoUpdate.getLatitude()).append(",").
                        append(infoUpdate.getLongitude()).append(",").
                        append(infoUpdate.getAltitude()).append('\n');
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void createLogBrain() {
        createCSVFile();
        startUpdatingCounter();
    }

    protected void closeLogBrain() {
        closeCSVFile();
    }
}


