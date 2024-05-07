package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

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

import com.dji.sdk.sample.demo.flightcontroller.VirtualStickView;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.OnScreenJoystick;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
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
import dji.sdk.gimbal.Gimbal;
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
    protected Button CameraControlbtn;
    protected Button CameraYawUp;
    protected Button CameraYawDown;
    protected Button CameraRollUp;
    protected Button CameraRollDown;
    protected Button CameraPitchUp;
    protected Button CameraPitchDown;
    private final Context context;
    public boolean isRecording = false;
    private boolean isRoute = false;
    private RelativeLayout waypointButtonsLayout;
    private Timer sendVirtualStickDataTimer;
    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;
    private FlightController flightController = null;
    private float waypoint_alt = 0;
    private int pitch_adjust = 0;
    private int yaw_adjust = 0;
    private int roll_adjust = 0;
    private int counter = 0;
    private int copy_counter = 0;
    private boolean isGoTo= true;


    public ILMButtons(Context context, View view) {
        goTobtn = view.findViewById(R.id.btn_ILM_GoTo);
        stopbtn = view.findViewById(R.id.btn_ILM_Stop);
        landbtn = view.findViewById(R.id.btn_ILM_Land);
        takeOffbtn = view.findViewById(R.id.btn_ILM_Take_Off);
        recordbtn = view.findViewById(R.id.btn_ILM_Record);
        Waypointbtn = view.findViewById(R.id.btn_ILM_Waypoint);
        AddWaypointbtn = view.findViewById(R.id.btn_ILM_Add_Waypoint);
        RepeatRoutebtn = view.findViewById(R.id.btn_ILM_Repeat_Route);
        CameraControlbtn = view.findViewById(R.id.btn_ILM_camera_controls);
        CameraYawUp =  view.findViewById(R.id.btn_ILM_camera_yaw_up);
        CameraYawDown = view.findViewById(R.id.btn_ILM_camera_yaw_down);
        CameraRollUp = view.findViewById(R.id.btn_ILM_camera_roll_up);
        CameraRollDown = view.findViewById(R.id.btn_ILM_camera_roll_down);
        CameraPitchUp = view.findViewById(R.id.btn_ILM_camera_pitch_up);
        CameraPitchDown = view.findViewById(R.id.btn_ILM_camera_pitch_down);

        waypointButtonsLayout = (RelativeLayout) view.findViewById(R.id.waypointButtonsLayout);
        if(flightController == null){
            if(ModuleVerificationUtil.isFlightControllerAvailable()){
                flightController = DJISampleApplication.getAircraftInstance().getFlightController();
            }
        }

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
        isGoTo = false;
//        flightController.sendVirtualStickFlightControlData(new FlightControlData(0, 0, yaw, waypoint_alt), // Only adjust yaw to rotate
//                new CommonCallbacks.CompletionCallback() {
//                    @Override
//                    public void onResult(DJIError djiError) {
//                        if (djiError != null) {
//                            Log.e("TAG", "Rotation failed: " + djiError.getDescription());
//                        } else {
//                            Log.d("TAG", "Rotation successful");
//                        }
//                    }
//                });

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


    protected synchronized void goTo(ILMWaypoints ilmWaypoints) {
        if (counter == 0 || copy_counter == 0) {
            showToast("Please add waypoints first");
            return;
        }

        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Virtual sticks enabled!");
                } else showToast("nope" + djiError);
            }
        });
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

        double lat = Double.parseDouble(ilmWaypoints.getWaypoints().get("Latitude" + (copy_counter - 1)));
        double lon = Double.parseDouble(ilmWaypoints.getWaypoints().get("Longitude" + (copy_counter - 1)));
        double alt = Double.parseDouble(ilmWaypoints.getWaypoints().get("Altitude" + (copy_counter - 1)));
        copy_counter--;
        final double[] distance = {Integer.MAX_VALUE};
        distance[0] = ilmWaypoints.haversine(lat, lon, alt);
        double angle = ilmWaypoints.calculateBearing(lat, lon);
        Log.e("Distance", String.valueOf(distance[0]));

        if (angle > 180)
            yaw = (float) (-360 + angle);
        if (angle < -180)
            yaw = (float) (360 + angle);
        if(angle < 180 && angle > -180)
            yaw = (float) angle;
        Log.e("yaw", String.valueOf(angle));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int executionCount = 0;
            @Override
            public void run() {
                if (executionCount < 6 && isGoTo) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(0, 0, yaw, (float) alt), // Only adjust yaw to rotate
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

                    double angle = ilmWaypoints.calculateBearing(lat, lon);

                    if (angle > 180)
                        yaw = (float) (-360 + angle);
                    if (angle < -180)
                        yaw = (float) (360 + angle);
                    if(angle < 180 && angle > -180)
                        yaw = (float) angle;

                    Log.e("yaw", String.valueOf(yaw));
                    Log.e("angle", String.valueOf(angle));

                    executionCount++;
                } else {
                    // Cancel the timer after executing the task 5 times
                    timer.cancel();
                }
            }
        }, 0, 150);

        pitch = (float) (Math.sin(Math.toRadians(yaw)) * 2); // Adjust speed as needed
        roll = (float) (Math.cos(Math.toRadians(yaw)) * 2);
        double currentAltitude = flightController.getState().getAircraftLocation().getAltitude();
        throttle = (float) ((alt - currentAltitude) * 0.2); // Adjust altitude

        Timer forward_timer = new Timer();
        final double[] finalDistance = {distance[0]};
        forward_timer.schedule(new TimerTask() {
            int executionCount = 0;

            @Override
            public void run() {
                if (finalDistance[0] > 0.001 && isGoTo) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(0, 2, yaw, (float) alt),
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                        Log.e("TAG", "Forward failed: " + djiError.getDescription());
                                    } else {
                                        Log.d("TAG", "Forward successful");
                                    }
                                }
                            });
                } else {
                    // Cancel the timer after executing the task 5 times
                    forward_timer.cancel();
                    flightController.setVirtualStickModeEnabled(false, null);
                    isGoTo = true;
                }
                finalDistance[0] = ilmWaypoints.haversine(lat, lon, alt);
                Log.e("Distance", String.valueOf(finalDistance[0]));
            }
        }, 1500, 250);
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
        counter++;
        copy_counter = counter;
    }

    public void RepeatRoute(ILMWaypoints ilmWaypoints) {
        HashMap<String, String> waypoints = ilmWaypoints.getWaypoints();
        if (waypoints != null && !waypoints.isEmpty()) {
            copy_counter = ilmWaypoints.getCounter();
            int time = 0;
            double distance = 0;
            for (int i = 0; i < copy_counter; i++) {
                Timer timer = new Timer();
                if(i != 0) {
                    double lat = Double.parseDouble(ilmWaypoints.getWaypoints().get("Latitude" + i));
                    double lon = Double.parseDouble(ilmWaypoints.getWaypoints().get("Longitude" + i ));
                    double alt = Double.parseDouble(ilmWaypoints.getWaypoints().get("Altitude" + i));
                    distance = ilmWaypoints.haversine(lat, lon, alt);
                    time = (int)distance/20*1000 + 1000;
                }
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        goTo(ilmWaypoints);
                    }
                }, time);
            }
        } else {
            Toast.makeText(context, "No route to repeat", Toast.LENGTH_SHORT).show();
        }
    }

    public void cameraControl(String str, char symbol){
        Gimbal gimbal = DJISDKManager.getInstance().getProduct().getGimbal();
        if (gimbal != null) {
            switch (str){
                case "yaw":
                    if(symbol == '+'){
                        yaw_adjust += 1;
                    }else if(symbol == '-'){
                        yaw_adjust -= 1;
                    }
                    gimbal.rotate(new Rotation.Builder().yaw(yaw_adjust)
                            .mode(RotationMode.ABSOLUTE_ANGLE)
                            .build(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.d("Gimbal", "Yaw rotation successful");
                            } else {
                                Log.e("Gimbal", "Yaw rotation failed: " + djiError.getDescription());
                            }
                        }
                    });
                    break;
                case "roll":
                    if(symbol == '+'){
                        roll_adjust += 1;
                    }else if(symbol == '-'){
                        roll_adjust -= 1;
                    }
                    gimbal.rotate(new Rotation.Builder().roll(roll_adjust)
                            .mode(RotationMode.ABSOLUTE_ANGLE)
                            .build(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.d("Gimbal", "Roll rotation successful");
                            } else {
                                Log.e("Gimbal", "Roll rotation failed: " + djiError.getDescription());
                            }
                        }
                    });
                    break;
                case "pitch":
                    if(symbol == '+'){
                        pitch_adjust += 1;
                    }else if(symbol == '-'){
                        pitch_adjust -= 1;
                    }
                    gimbal.rotate(new Rotation.Builder().pitch(pitch_adjust)
                            .mode(RotationMode.ABSOLUTE_ANGLE)
                            .build(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.d("Gimbal", "Pitch rotation successful");
                            } else {
                                Log.e("Gimbal", "Pitch rotation failed: " + djiError.getDescription());
                            }
                        }
                    });
                    break;
            }
        }
    }
}