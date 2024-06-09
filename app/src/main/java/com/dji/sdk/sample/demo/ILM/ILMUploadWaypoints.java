package com.dji.sdk.sample.demo.ILM;
import android.content.Context;
import android.util.Log;

import java.io.*;

import java.util.HashMap;
import java.util.Scanner;
public class ILMUploadWaypoints {
    private Context context;
    public ILMUploadWaypoints(Context context) {
        this.context = context;
    }
    public void readCSV(ILMWaypoints ilmWaypoints, ILMMapController ilmMapController) {
        HashMap<String, String> waypoints = new HashMap<>();
        ilmWaypoints.setWaypoints(waypoints);
        String line = "";
        String splitBy = ",";
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("route.csv")));
            int counter = 0;
            while ((line = br.readLine()) != null)
            {
                if (counter!=0) {
                    String[] waypoint = line.split(splitBy);
                    ilmWaypoints.addWaypoint(waypoint[1], waypoint[0], String.valueOf(8), String.valueOf(0), counter-1);
                    ilmMapController.addWaypoint(waypoint[1], waypoint[0], String.valueOf(8));
//                    waypoints.put("Longitude" + (counter-1), waypoint[0]);
//                    waypoints.put("Latitude" + (counter-1), waypoint[1]);
//                    waypoints.put("Altitude" + (counter-1), String.valueOf(8));
//                    waypoints.put("Pitch" + (counter-1), String.valueOf(0));
                    Log.e("Latitude" + (counter-1), waypoint[0]);
                    Log.e("Longitude" + (counter-1), waypoint[1]);
                    Log.e("Altitude" + (counter-1), String.valueOf(8));
                    Log.e("Pitch" + (counter-1), String.valueOf(0));
//                    Log.e("waypoint added", waypoint.toString());
                }
                counter++;
            }
            ilmWaypoints.setCounter(counter-1);
            Log.e("size: ", String.valueOf(ilmWaypoints.getWaypoints().size()/4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
