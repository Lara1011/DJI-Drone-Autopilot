package com.dji.sdk.sample.demo.ILM;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;

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
import dji.sdk.flightcontroller.FlightController;

public class ILMWaypoints {
    private Context context;
    private FileWriter writer;
    private static ILMInfoUpdate infoUpdate;
    private HashMap<String, String> waypoints = new HashMap<String, String>();
    //private List<Waypoint> waypoints2 = new ArrayList<Waypoint>();
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
        //waypoints2.add(new Waypoint(Double.parseDouble(infoUpdate.getLatitude()), Double.parseDouble(infoUpdate.getLongitude()), Float.parseFloat(infoUpdate.getAltitude())));
        counter++;
    }

    public double haversine(double lat2, double lon2, double alt2) {

        double lat1 = Double.parseDouble(infoUpdate.getLatitude());
        double lon1 = Double.parseDouble(infoUpdate.getLongitude());
        double alt1 = Double.parseDouble(infoUpdate.getAltitude());

        // Convert latitude and longitude from degrees to radians
        double R = 6371.0 + (alt1 + alt2) / 2; // Earth radius in kilometers, taking average altitude

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

    public double calculateBearing(double lat2, double lon2) {
        FlightController flightController = DJISampleApplication.getAircraftInstance().getFlightController();
        double lat1 = Double.parseDouble(String.valueOf(flightController.getState().getAircraftLocation().getLatitude()));
        double lon1 = Double.parseDouble(String.valueOf(flightController.getState().getAircraftLocation().getLongitude()));


        double deltaLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double bearing = Math.atan2(y, x);

        // Convert radians to degrees
        bearing = Math.toDegrees(bearing);

        // Normalize bearing to range [0, 360)
        bearing = (bearing + 360) % 360;

        return bearing;
    }

    public HashMap<String, String> getWaypoints(){
        return waypoints;
    }

    public int getCounter(){
        return counter;
    }

    public double getAltitude(){
        return Double.parseDouble(infoUpdate.getAltitude());
    }

    protected void createLogBrain() {
        createCSVFile();
    }

    protected void closeLogBrain() {
        closeCSVFile();
    }
}
