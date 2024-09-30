package com.dji.sdk.sample.demo.ILM;

import android.content.Context;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ILM_CSVLog {
    private Context context;
    private FileWriter writer;
    private Timer counterTimer;
    private ILM_StatusBar statusBar;

    public ILM_CSVLog(Context context, ILM_StatusBar statusBar) {
        this.context = context;
        this.statusBar = statusBar;
    }

    private void createCSVFile() {
        String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = "ILM_DJI_Drone_Data_" + currentDate + ".csv";

        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "DJI_Drone_Logs");
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, filename);

        try {
            writer = new FileWriter(file, true); // Append mode
            writer.append("Date,Time,Battery,Speed,Distance,X,Y,Z,Pitch,Roll,Yaw,Latitude,Longitude,Altitude,Mode").append('\n'); // Header row
            Toast.makeText(context, "SUCCESS: CSV File Created At " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "ERROR: Failed To Create CSV File", Toast.LENGTH_LONG).show();

        }
    }

    private void closeCSVFile() {
        if (writer != null) {
            try {
                writer.close();
                writer = null; // Reset the writer
                Toast.makeText(context, "SUCCESS: CSV File Closed.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "ERROR: Closing CSV File: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "ERROR: No CSV File To Close.", Toast.LENGTH_LONG).show();
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
                writer.append(statusBar.getDate()).append(",").
                        append(statusBar.getBattery()).append(",").
                        append(statusBar.getSpeed()).append(",").
                        append(statusBar.getDistance()).append(",").
                        append(statusBar.getILMX()).append(",").
                        append(statusBar.getILMY()).append(",").
                        append(statusBar.getILMZ()).append(",").
                        append(statusBar.getPitch()).append(",").
                        append(statusBar.getRoll()).append(",").
                        append(statusBar.getYaw()).append(",").
                        append(statusBar.getLatitude()).append(",").
                        append(statusBar.getLongitude()).append(",").
                        append(statusBar.getAltitude()).append('\n');
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