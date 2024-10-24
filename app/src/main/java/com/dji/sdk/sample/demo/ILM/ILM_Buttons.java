package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dji.sdk.sample.R;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import org.osmdroid.views.MapView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.common.util.CommonCallbacks.CompletionCallback;
import dji.sdk.flightcontroller.FlightController;
import dji.common.mission.waypoint.Waypoint;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.sdkmanager.DJISDKManager;

public class ILM_Buttons {
    private Context context;
    private View view;
    private FlightController flightController = ModuleVerificationUtil.getFlightController();
    private ILM_AdjustCamera cameraAdjust = new ILM_AdjustCamera();
    private ILM_GoTo goTo;
    private ILM_GoTo goToMissions;
    private final ILM_FollowMe followMe;

    protected boolean isRecording = false;
    private CountDownTimer recordingTimer;
    private long recordingTimeMillis = 0;
    protected TextView recordText;

    protected Button returnToHomeBtn, repeatRouteBtn;
    //    protected Button panicStopBtn;
    protected Button landBtn, takeOffBtn, goToBtn, followMeBtn, stopBtn, enableVirtualStickBtn, recordBtn;
    protected Button waypointBtn, addWaypointBtn, removeWaypointBtn;
    protected Button cameraAdjustBtn, adjustPitchPlusBtn, adjustPitchMinusBtn, adjustRollPlusBtn, adjustRollMinusBtn, adjustYawPlusBtn, adjustYawMinusBtn;

    protected Button missionsBtn, mission1Btn, mission2Btn, mission3Btn;
    protected Button waypointsBtn, waypoint1Btn, waypoint2Btn, waypoint3Btn, waypoint4Btn, waypoint5Btn, waypoint6Btn, waypoint7Btn, waypoint8Btn;

    protected Button mapResizeBtn, mapCenterBtn;

    private int pitch_adjust;
    private int yaw_adjust;
    private int roll_adjust;

    protected int count = 0;
    protected int setCounter = 0;
    private int counter = 0;

    protected Button peopleDetectionBtn;


    private CompletionCallback createCallback(final String action) {
        return new CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    ToastUtils.setResultToToast(action);
                } else {
                    ToastUtils.setResultToToast(djiError.getDescription());
                }
            }
        };
    }

    public ILM_Buttons(Context context, View view) {
        this.context = context;
        this.view = view;
        this.followMe = new ILM_FollowMe(context);
        initUI();
        panicStop();
    }

    private void initUI() {
        returnToHomeBtn = view.findViewById(R.id.btn_ILM_ReturnToHome);
        repeatRouteBtn = view.findViewById(R.id.btn_ILM_RepeatRoute);

        peopleDetectionBtn = view.findViewById(R.id.btn_ILM_PeopleDetection);

        mapResizeBtn = view.findViewById(R.id.btn_ILM_MapResize);
        mapCenterBtn = view.findViewById(R.id.btn_ILM_MapCenter);

        landBtn = view.findViewById(R.id.btn_ILM_Land);
        takeOffBtn = view.findViewById(R.id.btn_ILM_Take_Off);
        goToBtn = view.findViewById(R.id.btn_ILM_GoTo);
        followMeBtn = view.findViewById(R.id.btn_ILM_FollowMe);
        stopBtn = view.findViewById(R.id.btn_ILM_Stop);
//        panicStopBtn = view.findViewById(R.id.btn_ILM_Panic_Stop);
        enableVirtualStickBtn = view.findViewById(R.id.btn_ILM_Enable_VirtualStick);
        recordBtn = view.findViewById(R.id.btn_ILM_Record);
        recordText = view.findViewById(R.id.textView_ILM_Record);

        waypointBtn = view.findViewById(R.id.btn_ILM_Waypoint);
        addWaypointBtn = view.findViewById(R.id.btn_ILM_AddWaypoint);
        removeWaypointBtn = view.findViewById(R.id.btn_ILM_RemoveWaypoint);

        cameraAdjustBtn = view.findViewById(R.id.btn_ILM_CameraAdjust);
        adjustPitchPlusBtn = view.findViewById(R.id.btn_ILM_AdjustPitchPlus);
        adjustPitchMinusBtn = view.findViewById(R.id.btn_ILM_AdjustPitchMinus);
        adjustRollPlusBtn = view.findViewById(R.id.btn_ILM_AdjustRollPlus);
        adjustRollMinusBtn = view.findViewById(R.id.btn_ILM_AdjustRollMinus);
        adjustYawPlusBtn = view.findViewById(R.id.btn_ILM_AdjustYawPlus);
        adjustYawMinusBtn = view.findViewById(R.id.btn_ILM_AdjustYawMinus);

        missionsBtn = view.findViewById(R.id.btn_ILM_Missions);
        mission1Btn = view.findViewById(R.id.btn_ILM_Mission_1);
        mission2Btn = view.findViewById(R.id.btn_ILM_Mission_2);
        mission3Btn = view.findViewById(R.id.btn_ILM_Mission_3);

        waypointsBtn = view.findViewById(R.id.btn_ILM_Waypoints);
        waypoint1Btn = view.findViewById(R.id.btn_ILM_Waypoint_1);
        waypoint2Btn = view.findViewById(R.id.btn_ILM_Waypoint_2);
        waypoint3Btn = view.findViewById(R.id.btn_ILM_Waypoint_3);
        waypoint4Btn = view.findViewById(R.id.btn_ILM_Waypoint_4);
        waypoint5Btn = view.findViewById(R.id.btn_ILM_Waypoint_5);
        waypoint6Btn = view.findViewById(R.id.btn_ILM_Waypoint_6);
        waypoint7Btn = view.findViewById(R.id.btn_ILM_Waypoint_7);
        waypoint8Btn = view.findViewById(R.id.btn_ILM_Waypoint_8);

        pitch_adjust = cameraAdjust.getPitch();
        yaw_adjust = cameraAdjust.getYaw();
        roll_adjust = cameraAdjust.getRoll();

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record();
            }
        });
    }

    public void addWaypoint(ILM_Waypoints waypoints, ILM_MapController mapController, ILM_AllWaypoints allWaypoints) {
        if (goTo == null)
            goTo = new ILM_GoTo(waypoints, mapController);
        waypoints.updateCSVInfo(mapController);
        HashMap<String, String> lastWaypoint = waypoints.getWaypoints();
        if(count == 0) {
            if (counter < 8) {
                allWaypoints.addWaypoint(lastWaypoint.get("Latitude" + (waypoints.getCounter() - 1)), lastWaypoint.get("Longitude" + (waypoints.getCounter() - 1)), lastWaypoint.get("Altitude" + (waypoints.getCounter() - 1)), String.valueOf(0), counter);
            }
        }
        counter++;
    }

    public void removeWaypoint(ILM_Waypoints waypoints, ILM_MapController mapController) {
        waypoints.removeWaypoint(mapController);
        ToastUtils.setResultToToast("Waypoint Removed!");
    }

    protected void returnToHome() {
        flightController.startGoHome(createCallback("Returning To Home!"));
    }

    protected void takeOff() {
        flightController.startTakeoff(createCallback("Taking Off!"));
    }

    protected void stop() {
        if (flightController != null) {
            flightController.setVirtualStickModeEnabled(false, null);
            flightController.cancelGoHome(null);
            flightController.cancelTakeoff(null);
            flightController.cancelLanding(createCallback("Drone Stopped!"));
        }
        if (goTo != null) {
            goTo.isGoTo = false;
            goTo.isRepeatRoute = false;
            goTo.resetCount();
        }
        if (goToMissions != null) {
            goToMissions.isGoTo = false;
            goToMissions.isRepeatRoute = false;
            goToMissions.resetCount();
        }
        Log.e("stop", "isGoTo.....................");
        if (followMe.isFollowMe) {
            Log.e("stop", "followme.....................");
            followMe.stopFollowing();
            Log.e("stop", "followme.....................");
        }
    }

    protected void panicStop() {
        //ToDo: Ask Boaz about it
        if (flightController != null) {
            flightController.setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, createCallback("Panic Mode Enabled"));
        }
    }

    protected void land() {
        flightController.startLanding(createCallback("Landing!"));
    }

    protected synchronized void goTo(ILM_Waypoints waypoints, ILM_MapController mapController, boolean isMission) {
        if (waypoints.getWaypoints().isEmpty()) {
            showToast("Please add waypoints first");
            return;
        }
        if (goTo == null) {
            goTo = new ILM_GoTo(waypoints, mapController);
        }
        goTo.setMode(1);
        double lat = Double.parseDouble(waypoints.getWaypoints().get("Latitude" + count));
        double lon = Double.parseDouble(waypoints.getWaypoints().get("Longitude" + count));
        double alt = Double.parseDouble(waypoints.getWaypoints().get("Altitude" + count));
        count = setCounter;
        Log.e("&Altitude", String.valueOf(alt));
        Log.e("&Latitude", String.valueOf(lat));
        Log.e("&Longitude", String.valueOf(lon));
        if (isMission){
            goToMissions.isGoTo = true;
            goToMissions.setWaypoint(lat, lon, alt);
            goToMissions.goTo();
        }else {
            goTo.isGoTo = true;
            goTo.setWaypoint(lat, lon, alt);
            goTo.goTo();
        }
    }

    protected void followMe() {
        followMe.FollowMe();
    }

    public void EnableVirtualStick() {
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                flightController.setVirtualStickAdvancedModeEnabled(true);
                if (djiError != null) {
                    ToastUtils.setResultToToast(djiError.toString());
                }
//                DialogUtils.showDialogBasedOnError(context, djiError);
            }
        });
    }

    public void record() {
        if (isRecording) {
            startRecording();
            startRecordingTimer();

        } else {
            stopRecording();
            recordText.setText("Start Recording");
            stopRecordingTimer();
        }
    }

    private void startRecordingTimer() {
        recordingTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                recordingTimeMillis += 1000;
                updateTimerText();
            }

            @Override
            public void onFinish() {
            }
        };
        recordingTimer.start();
    }

    private void stopRecordingTimer() {
        if (recordingTimer != null) {
            recordingTimer.cancel();
            recordingTimeMillis = 0;
        }
    }

    private void updateTimerText() {
        long minutes = (recordingTimeMillis / 1000) / 60;
        long seconds = (recordingTimeMillis / 1000) % 60;
        recordText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    protected void startRecording() {
        if (isRecording) {
            DJISampleApplication.getProductInstance().getCamera().startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Recording started");
                        isRecording = true;
                        recordBtn.setBackgroundResource(R.drawable.ilm_drone_capture_video_on);
                    } else {
                        showToast("Failed to start recording: " + djiError.getDescription());
                    }
                }
            });

        } else {
            showToast("Camera is already recording.");
        }
    }

    protected void stopRecording() {
        DJISampleApplication.getProductInstance().getCamera().stopRecordVideo(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Recording stopped");
                    isRecording = false;
                    recordBtn.setBackgroundResource(R.drawable.ilm_drone_capture_video_off);
                } else {
                    showToast("Failed to stop recording: " + djiError.getDescription());
                }
            }
        });
    }

    protected void waypointBtn() {
        LinearLayout waypointLayout = view.findViewById(R.id.layout_ILM_AddRemoveWaypoint);
        LinearLayout expandedMenuLayout = view.findViewById(R.id.layout_expandedMenuLayout);

        if (waypointLayout.getVisibility() == View.VISIBLE) {
            waypointLayout.setVisibility(View.GONE);
            expandedMenuLayout.setVisibility(View.GONE);
        } else {
            waypointLayout.setVisibility(View.VISIBLE);
            expandedMenuLayout.setVisibility(View.VISIBLE);
        }
    }

    //btn_ILM_Missions
    protected void missionListBtn() {
        ScrollView missionsList = view.findViewById(R.id.scrollView_missionsList);
        TableLayout LatLonAlt = view.findViewById(R.id.tableRow_ILM_LatLonAlt);
        if (missionsList.getVisibility() == View.VISIBLE) {
            missionsList.setVisibility(View.GONE);
            LatLonAlt.setVisibility(View.VISIBLE);
        } else {
            missionsList.setVisibility(View.VISIBLE);
            LatLonAlt.setVisibility(View.GONE);
        }
    }

    //btn_ILM_Waypoints
    protected void waypointsBtn() {
        ScrollView waypointsList = view.findViewById(R.id.scrollView_waypointsList);
        TableLayout LatLonAlt = view.findViewById(R.id.tableRow_ILM_LatLonAlt);
        if (waypointsList.getVisibility() == View.VISIBLE) {
            waypointsList.setVisibility(View.GONE);
            LatLonAlt.setVisibility(View.VISIBLE);
        } else {
            waypointsList.setVisibility(View.VISIBLE);
            LatLonAlt.setVisibility(View.GONE);
        }
    }

    protected void cameraAdjustVisibility() {
        LinearLayout cameraAdjustLayout = view.findViewById(R.id.layout_ILM_AdjustPitchRollYaw);
        if (cameraAdjustLayout.getVisibility() == View.VISIBLE) {
            cameraAdjustLayout.setVisibility(View.GONE);
        } else {
            cameraAdjustLayout.setVisibility(View.VISIBLE);
        }
    }

    public void cameraAdjust(String str, char symbol) {
        Gimbal gimbal = DJISDKManager.getInstance().getProduct().getGimbal();
        if (gimbal != null) {
            switch (str) {
                case "yaw":
                    if (symbol == '+') {
                        yaw_adjust += 1;
                    } else if (symbol == '-') {
                        yaw_adjust -= 1;
                    }
                    cameraAdjust.setYaw(yaw_adjust);
                    break;
                case "roll":
                    if (symbol == '+') {
                        roll_adjust += 1;
                    } else if (symbol == '-') {
                        roll_adjust -= 1;
                    }
                    cameraAdjust.setRoll(roll_adjust);
                    break;
                case "pitch":
                    if (symbol == '+') {
                        pitch_adjust += 1;
                    } else if (symbol == '-') {
                        pitch_adjust -= 1;
                    }
                    cameraAdjust.setPitch(pitch_adjust);
                    break;
            }
        }
    }

    public void RepeatRoute(ILM_Waypoints waypoints, ILM_MapController mapController, boolean isMission) {
        if (goTo == null)
            goTo = new ILM_GoTo(waypoints, mapController);
        if (isMission) {
            goToMissions = new ILM_GoTo(waypoints, mapController);
            goToMissions.setMode(1);
            goToMissions.isRepeatRoute = true;
            goTo(waypoints, mapController, true);
        }
        else {
            goTo.setMode(1);
            goTo.isRepeatRoute = true;
            goTo(waypoints, mapController, false);
        }
    }

    public void mapResize(boolean isExpanded, MapView mapView) {
        ViewGroup.LayoutParams params = mapView.getLayoutParams();
        if (isExpanded) {
            // Set to small size
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources().getDisplayMetrics());
        } else {
            // Set to expanded size
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, context.getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        }
        mapView.setLayoutParams(params);

    }

    public void up(){
        float yaw = flightController.getYawControlMode().value();
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Virtual sticks enabled!");
                } else showToast("Failed to enable virtual sticks: " + djiError);
            }
        });
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            float alt = flightController.getState().getAircraftLocation().getAltitude();
            @Override
            public void run() {
                if (flightController.getState().getAircraftLocation().getAltitude() < alt + 5) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(0, 0, yaw, (float) (alt + 5.1)), // Only adjust yaw to rotate
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e("TAG", "Rotation failed: " + djiError.getDescription());
                                    } else {
                                        Log.d("TAG", "Rotation successful");
                                    }
                                }
                            });
                } else {
                    // Cancel the timer after executing the task 5 times
                    timer.cancel();
                    flightController.setVirtualStickModeEnabled(false, null);
                }
            }
        }, 0, 150);
    }

    public void down(){
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Virtual sticks enabled!");
                } else showToast("Failed to enable virtual sticks: " + djiError);
            }
        });
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            float yaw = flightController.getYawControlMode().value();
            float alt = flightController.getState().getAircraftLocation().getAltitude();
            @Override
            public void run() {
                if (flightController.getState().getAircraftLocation().getAltitude() > alt - 5) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(0, 0, yaw, alt - 5), // Only adjust yaw to rotate
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e("TAG", "Rotation failed: " + djiError.getDescription());
                                    } else {
                                        Log.d("TAG", "Rotation successful");
                                    }
                                }
                            });
                } else {
                    // Cancel the timer after executing the task 5 times
                    timer.cancel();
                    flightController.setVirtualStickModeEnabled(false, null);
                }
            }
        }, 0, 150);
    }
}

