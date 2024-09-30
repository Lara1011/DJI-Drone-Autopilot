package com.dji.sdk.sample.demo.ILM;

import android.content.Context;

import java.util.HashMap;

public class ILM_AllWaypoints extends ILM_Waypoints {
    HashMap<String, String> waypoints;

    public ILM_AllWaypoints(Context context, ILM_StatusBar statusBar) {
        super(context, statusBar);
        waypoints = new HashMap<>();
    }

    public void setAllWaypoints() {
        HashMap<String, String> current_waypoints = super.getWaypoints();
        for (int i = 0; i < current_waypoints.size(); i++) {
            waypoints.put("Waypoint" + i, current_waypoints.get("Latitude" + i) + "," + current_waypoints.get("Longitude" + i) + "," + current_waypoints.get("Altitude" + i));
        }
    }

    public HashMap<String, String> getAllWaypoints() {
        return waypoints;
    }
}