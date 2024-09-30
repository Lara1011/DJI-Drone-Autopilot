package com.dji.sdk.sample.demo.ILM;

import android.content.Context;

import java.io.*;

import java.util.HashMap;

public class ILM_UploadWaypoints {
    private Context context;
    private String fileName;

    public ILM_UploadWaypoints(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public void readCSV(ILM_Waypoints waypoints, ILM_MapController mapController) {
        HashMap<String, String> wpoints = new HashMap<>();
        waypoints.setWaypoints(wpoints);
        String line;
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName + ".csv")));
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter != 0) {
                    String[] waypoint = line.split(splitBy);
                    waypoints.addWaypoint(waypoint[1], waypoint[0], String.valueOf(8), String.valueOf(0), counter - 1);
                    mapController.addWaypoint(waypoint[1], waypoint[0], String.valueOf(8));
                }
                counter++;
            }
            waypoints.setCounter(counter - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
