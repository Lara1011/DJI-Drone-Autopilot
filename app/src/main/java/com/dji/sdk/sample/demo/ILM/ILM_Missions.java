package com.dji.sdk.sample.demo.ILM;

import android.content.Context;

import java.util.HashMap;

public class ILM_Missions {
    private HashMap<String, ILM_Waypoints> missions;
    private Context conext;
    private ILM_StatusBar statusBar;
    private ILM_MapController mapController;

    public ILM_Missions(Context context, ILM_StatusBar statusBar, ILM_MapController mapController) {
        missions = new HashMap<>();
        this.conext = context;
        this.statusBar = statusBar;
        this.mapController = mapController;
    }

    public HashMap<String, ILM_Waypoints> getMissions() {
        return missions;
    }

    public void setMissions(HashMap<String, ILM_Waypoints> missions) {
        this.missions = missions;
    }

    public void addMission(int i) {
        ILM_Waypoints waypoints = new ILM_Waypoints(conext, statusBar);
        ILM_UploadWaypoints ilmUploadWaypoints = new ILM_UploadWaypoints(conext, "m" + i);
        ilmUploadWaypoints.readCSV(waypoints, mapController);
        missions.put("m" + i, waypoints);
    }
    public void clearMissions() {
        for (ILM_Waypoints waypoints : missions.values()) {
            for (int i = 0; i < waypoints.getWaypoints().size(); i++) {
                waypoints.removeWaypoint(mapController);
            }
        }
        missions.clear();
    }
    public ILM_Waypoints loadMission(int i) {
        return missions.get("m" + i);
    }
}
