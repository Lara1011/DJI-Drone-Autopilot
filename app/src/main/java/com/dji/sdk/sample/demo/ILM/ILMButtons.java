package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dji.sdk.sample.R;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypoint.WaypointTurnMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;


public class ILMButtons {
    protected Button goTobtn;
    protected Button stopbtn;
    protected Button landbtn;
    protected Button takeOffbtn;
    protected Button recordbtn;
    protected Button Waypointbtn;
    protected Button AddWaypointbtn;
    protected Button RepeatRoutebtn;
    private final Context context;
    public boolean isRecording = false;
    private boolean isRoute = false;
    private RelativeLayout waypointButtonsLayout;
    private WaypointMission.Builder builder;
    String latitude = "32";
    String longitude = "35";
    String altitude = "0";
    String pitch = "0";

    private WaypointMissionOperator waypointMissionOperator = null;
    private WaypointMission mission = null;
    private WaypointMissionOperatorListener listener;
    private final static int WAYPOINT_COUNT = 4;

    FlightController flightController = ModuleVerificationUtil.getFlightController();


    public ILMButtons(Context context, View view) {
        goTobtn = view.findViewById(R.id.btn_ILM_GoTo);
        stopbtn = view.findViewById(R.id.btn_ILM_Stop);
        landbtn = view.findViewById(R.id.btn_ILM_Land);
        takeOffbtn = view.findViewById(R.id.btn_ILM_Take_Off);
        recordbtn = view.findViewById(R.id.btn_ILM_Record);
        Waypointbtn = view.findViewById(R.id.btn_ILM_Waypoint);
        AddWaypointbtn = view.findViewById(R.id.btn_ILM_Add_Waypoint);
        RepeatRoutebtn = view.findViewById(R.id.btn_ILM_Repeat_Route);

        waypointButtonsLayout = (RelativeLayout) view.findViewById(R.id.waypointButtonsLayout);

        this.context = context;
    }


    protected void takeOff() {
        flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
    }


    protected void stop() {
        flightController.cancelGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
        flightController.cancelTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
        flightController.cancelLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });

//        if(waypointMissionOperator != null){
//            waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(DJIError djiError) {
//                    ToastUtils.setResultToToast(djiError != null ? "" : djiError.getDescription());
//                }
//            });
//        }
//        flightController.getFlightAssistant().setLandingProtectionEnabled(true, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError djiError) {
//                if (djiError == null) {
//                    showToast(context.getResources().getString(R.string.success));
//                    disable(flightController);
//                } else {
//                    showToast(djiError.getDescription());
//                }
//            }
//        });
    }


    protected void land() {
        flightController.startLanding(new CommonCallbacks.CompletionCallback(){
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
    }

    protected void goTo(ILMWaypoints ilmWaypoints) {
        waypointMissionOperator = MissionControl.getInstance().getWaypointMissionOperator();
        setUpListener();
        mission = createMission(ilmWaypoints);
        if (mission != null) {
            Log.i("load", "started loading mission");
            waypointMissionOperator.getLoadedMissionBuilder();
            Log.i("Loaded mission: " , waypointMissionOperator.getLoadedMissionBuilder().toString());
            DJIError loadError = waypointMissionOperator.loadMission(mission);
            Log.i("load", loadError != null ? loadError.getDescription() : "Load success");
            if (loadError == null) {
                uploadMission();
            } else {
                // Print more details about the mission for debugging
                Log.e("Mission Details", "Mission: " + mission.toString());
            }
        } else {
            ToastUtils.setResultToToast("Mission is null");
        }
    }

    private void uploadMission() {
        if (WaypointMissionState.READY_TO_RETRY_UPLOAD.equals(waypointMissionOperator.getCurrentState())
                || WaypointMissionState.READY_TO_UPLOAD.equals(waypointMissionOperator.getCurrentState())) {
            waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError uploadError) {
                    ToastUtils.setResultToToast(uploadError != null ? uploadError.getDescription() : "upload success");

                    if (uploadError == null) {
                        // Mission uploaded successfully, proceed to start
                        startWaypointMission();
                    }
                }
            });
        } else {
            ToastUtils.setResultToToast("Wait for mission to be loaded");
        }
    }

    private void startWaypointMission() {
        if (waypointMissionOperator != null && mission != null) {
            waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    ToastUtils.setResultToToast(djiError != null ? djiError.getDescription() : "Start success");
                }
            });
        } else {
            ToastUtils.setResultToToast("Mission or mission operator is null");
        }
    }

    private void setUpListener() {
        // Example of Listener
        listener = new WaypointMissionOperatorListener() {
            @Override
            public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {
                // Example of Download Listener
                if (waypointMissionDownloadEvent.getProgress() != null
                        && waypointMissionDownloadEvent.getProgress().isSummaryDownloaded
                        && waypointMissionDownloadEvent.getProgress().downloadedWaypointIndex == (WAYPOINT_COUNT - 1)) {
                    ToastUtils.setResultToToast("Mission is downloaded successfully");
                }
            }

            @Override
            public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
                // Example of Upload Listener
                if (waypointMissionUploadEvent.getProgress() != null
                        && waypointMissionUploadEvent.getProgress().isSummaryUploaded
                        && waypointMissionUploadEvent.getProgress().uploadedWaypointIndex == (WAYPOINT_COUNT - 1)) {
                    ToastUtils.setResultToToast("Mission is uploaded successfully");
                }
            }

            @Override
            public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
            }

            @Override
            public void onExecutionStart() {
                ToastUtils.setResultToToast("Mission started");
            }

            @Override
            public void onExecutionFinish(@Nullable DJIError djiError) {
            }
        };

        if (waypointMissionOperator != null) {
            waypointMissionOperator.addListener(listener);
        }
    }


    private WaypointMission createMission(ILMWaypoints ilmWaypoints){
        builder = new WaypointMission.Builder();
        builder.maxFlightSpeed(15)  // Set maximum flight speed
                .autoFlightSpeed(8)  // Set automatic flight speed
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .finishedAction(WaypointMissionFinishedAction.NO_ACTION)
                .headingMode(WaypointMissionHeadingMode.AUTO);
        HashMap<String, String> waypoints = ilmWaypoints.getWaypoints();
        if (isRoute) {
            for (int i = 0; i < waypoints.size(); i++) {
                latitude = waypoints.get("Latitude" + i);
                longitude = waypoints.get("Longitude" + i);
                altitude = waypoints.get("Altitude" + i);
                pitch = waypoints.get("Pitch" + i);

                Waypoint waypoint = new Waypoint(Double.parseDouble(latitude), Double.parseDouble(longitude), Float.parseFloat(altitude));
                waypoint.turnMode = WaypointTurnMode.CLOCKWISE;
                waypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, Integer.parseInt(pitch)));
                builder.addWaypoint(waypoint);
            }
            isRoute = false;
            return builder.build();
        } else {
            int size = waypoints.size();
            if (size > 0) {
                latitude = waypoints.get("Latitude" + (size/4 - 1));
                longitude = waypoints.get("Longitude" + (size/4 - 1));
                altitude = waypoints.get("Altitude" + (size/4 - 1));
                pitch = waypoints.get("Pitch" + (size/4 - 1));
//                waypoints.remove("Latitude" + (size/4 - 1));
//                waypoints.remove("Longitude" + (size/4 - 1));
//                waypoints.remove("Altitude" + (size/4 - 1));
//                waypoints.remove("Pitch" + (size/4 - 1));
            }
        }

        Waypoint waypoint = new Waypoint(Double.parseDouble(latitude), Double.parseDouble(longitude), Float.parseFloat(altitude));
        waypoint.turnMode = WaypointTurnMode.CLOCKWISE;
        waypoint.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, Integer.parseInt(pitch)));
        builder.addWaypoint(new Waypoint(32.1012839, 35.2020039, Float.parseFloat(altitude)));
        builder.addWaypoint(waypoint);

        Log.i("builder waypoints: " , builder.getWaypointList().toString());

        return builder.build();
    }

    private void stopOnPlace() {
        FlightControlData flightControlData = new FlightControlData(0,0,0,0);
        flightControlData.setVerticalThrottle(0);
        flightControlData.setRoll(0);
        flightControlData.setPitch(0);
        flightControlData.setYaw(0);

        FlightController flightController = DJISampleApplication.getAircraftInstance().getFlightController();
        if (flightController.isVirtualStickControlModeAvailable()) {
            flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            flightController.sendVirtualStickFlightControlData(flightControlData,new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                }
            });
        }
    }

    public void disable(FlightController flightController) {
        stopOnPlace();
        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
    }

    public void record() {
        if (isRecording) {
            startRecording();
            recordbtn.setBackgroundResource(R.drawable.ilmstoprecord);
        } else {
            stopRecording();
            recordbtn.setBackgroundResource(R.drawable.ilmstartrecord);
        }
    }

    private void startRecording() {
        if (isRecording) {
            DJISampleApplication.getProductInstance().getCamera().startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Recording started");
                        isRecording = true;
                    } else {
                        showToast("Failed to start recording: " + djiError.getDescription());
                    }
                }
            });
        } else {
            showToast("Camera is already recording.");
        }
    }

    private void stopRecording() {
        DJISampleApplication.getProductInstance().getCamera().stopRecordVideo(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Recording stopped");
                    isRecording = false;
                } else {
                    showToast("Failed to stop recording: " + djiError.getDescription());
                }
            }
        });
    }

    public void WaypointsList() {
        if (waypointButtonsLayout.getVisibility() == View.VISIBLE) {
            waypointButtonsLayout.setVisibility(View.GONE);
        } else {
            waypointButtonsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void AddWaypoint(ILMWaypoints ilmWaypoints) {
        ilmWaypoints.updateCSVInfo();
    }

    public void RepeatRoute(ILMWaypoints ilmWaypoints) {
        HashMap<String, String> waypoints = ilmWaypoints.getWaypoints();
        if (waypoints != null && !waypoints.isEmpty()) {
                goTo(ilmWaypoints);
        } else {
            Toast.makeText(context, "No route to repeat", Toast.LENGTH_SHORT).show();
        }
    }
}

