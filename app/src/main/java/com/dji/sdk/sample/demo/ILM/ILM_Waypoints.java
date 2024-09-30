package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class ILM_Waypoints {
    private Context context;
    private FileWriter writer;
    private HashMap<String, String> waypoints = new HashMap<String, String>();
    private ILM_StatusBar statusBar;
    private int counter = 0;

    public ILM_Waypoints(Context context, ILM_StatusBar statusBar) {
        this.context = context;
        this.statusBar = statusBar;
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

    protected void updateCSVInfo(ILM_MapController mapController) {
        if (writer != null) {
            try {
                writer.append(statusBar.getDate()).append(",").
                        append(statusBar.getLatitude()).append(",").
                        append(statusBar.getLongitude()).append(",").
                        append(statusBar.getAltitude()).append(',').
                        append(statusBar.getPitch()).append("\n");
                writer.flush();
                showToast("Waypoint added !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        waypoints.put("Latitude" + counter, statusBar.getLatitude());
        waypoints.put("Longitude" + counter, statusBar.getLongitude());
        waypoints.put("Altitude" + counter, statusBar.getAltitude());
        waypoints.put("Pitch" + counter, statusBar.getPitch());
        mapController.addWaypoint(statusBar.getLatitude(), statusBar.getLongitude(), statusBar.getAltitude());
        counter++;
    }

    public void addWaypoint(String latitude, String longitude, String altitude, String pitch, int count) {
        waypoints.put("Latitude" + count, latitude);
        waypoints.put("Longitude" + count, longitude);
        waypoints.put("Altitude" + count, altitude);
        waypoints.put("Pitch" + count, pitch);
        if (writer != null) {
            try {
                writer.append(statusBar.getDate()).append(",").
                        append(latitude).append(",").
                        append(longitude).append(",").
                        append(altitude).append(',').
                        append(pitch).append("\n");
                writer.flush();
                showToast("Waypoint added !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeWaypoint(ILM_MapController mapController) {
        if (waypoints.isEmpty())
            return;
        mapController.removeWaypoint(waypoints.get("Latitude" + (counter - 1)), waypoints.get("Longitude" + (counter - 1)), waypoints.get("Altitude" + (counter - 1)));
        waypoints.remove("Latitude" + (counter - 1));
        waypoints.remove("Longitude" + (counter - 1));
        waypoints.remove("Altitude" + (counter - 1));
        waypoints.remove("Pitch" + (counter - 1));
        counter--;
    }

    public HashMap<String, String> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(HashMap<String, String> waypoints) {
        this.waypoints = waypoints;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public double getAltitude() {
        return Double.parseDouble(statusBar.getAltitude());
    }

    protected void createLogBrain() {
        createCSVFile();
    }

    protected void closeLogBrain() {
        closeCSVFile();
    }
}
