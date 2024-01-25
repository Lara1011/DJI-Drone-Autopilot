package com.dji.sdk.sample.demo.ILM;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dji.common.mission.waypoint.Waypoint;

public class ILMWaypoints {
    private Context context;
    private FileWriter writer;
    private ILMInfoUpdate infoUpdate;
    private HashMap<String, String> waypoints = new HashMap<String, String>();
    private int counter = 0;

    public ILMWaypoints(Context context, ILMInfoUpdate infoUpdate) {
        this.context = context;
        this.infoUpdate = infoUpdate;
    }

    private void createCSVFile() {
        String currDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String filename = "ILM_DJI_Waypoints -" + currDate + ".csv";
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Waypoints");
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, filename);
        boolean isFileExists = file.exists();
        try {
            writer = new FileWriter(file, true);
            if (!isFileExists) {
                writer.append("Date,Time,Latitude,Longitude,Altitude,Pitch").append('\n');
            }
            Toast.makeText(context, "Waypoints file created at " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeCSVFile() {
        if (writer != null) {
            try {
                writer.close();
                writer = null;
                Toast.makeText(context, "Waypoints file closed.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "No Waypoints file to close.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void updateCSVInfo() {
        if (writer != null) {
            try {
                writer.append(infoUpdate.getDate()).append(",").
                        append(infoUpdate.getLatitude()).append(",").
                        append(infoUpdate.getLongitude()).append(",").
                        append(infoUpdate.getAltitude()).append(',').
                        append(infoUpdate.getPitch()).append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        waypoints.put("Latitude"+ counter, infoUpdate.getLatitude());
        waypoints.put("Longitude"+ counter, infoUpdate.getLongitude());
        waypoints.put("Altitude"+ counter, infoUpdate.getAltitude());
        waypoints.put("Pitch"+ counter, infoUpdate.getPitch());
        counter++;
    }

    public HashMap<String, String> getWaypoints(){
        return waypoints;
    }

    protected void createLogBrain() {
        createCSVFile();
    }

    protected void closeLogBrain() {
        closeCSVFile();
    }
}
