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

public class ILMGoToWaypoints {
    private Context context;
    private FileWriter writer;
    private Timer counterTimer;
    private ILMInfoUpdate infoUpdate;

    private int counter = 0;

    public ILMGoToWaypoints(Context context, ILMInfoUpdate infoUpdate) {
        this.context = context;
        this.infoUpdate = infoUpdate;
    }

    private void createCSVFile() {
        String currDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String filename = "ILM_DJI_GoTo_Waypoints -" + currDate + ".csv";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, filename);
        boolean isFileExists = file.exists();
        try {
            writer = new FileWriter(file, true); // Append mode
            if (!isFileExists) {
                writer.append("Date,Time,Latitude,Longitude,Altitude").append('\n'); // Header row
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

    protected void updateCSVInfo() {
        if (writer != null) {
            try {
                writer.append(String.valueOf(counter++)).append(infoUpdate.getDate()).append(",").
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
        updateCSVInfo();
    }

    protected void closeLogBrain() {
        closeCSVFile();
    }
}
