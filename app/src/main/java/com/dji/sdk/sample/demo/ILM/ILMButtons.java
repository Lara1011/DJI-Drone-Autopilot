package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.app.Notification;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dji.sdk.sample.R;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.mission.followme.FollowMeHeading;
import dji.common.mission.followme.FollowMeMission;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.midware.data.model.P3.DataOsdGetPushCommon;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.mission.followme.FollowMeMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.MissionControl;
import dji.sdk.products.Aircraft;

public class ILMButtons {
    protected Button goTobtn;
    protected Button stopbtn;
    protected Button landbtn;
    protected Button takeOffbtn;
    protected Button recordbtn;
    protected Button Waypointbtn;
    protected Button AddWaypointbtn;
    protected Button RepeatRoutebtn;
    private Context context;
    public boolean isRecording = false;
    private RelativeLayout waypointButtonsLayout;

    FlightController flightController = ModuleVerificationUtil.getFlightController();
//    private double lat;
//    private double lon;
//    private float alt;


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


//        lat = lat;
//        lon = lon;
//        alt = alt;
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


    protected void goTo() {
        FollowMeMissionOperator followMeMissionOperator = MissionControl.getInstance().getFollowMeMissionOperator();

// Assuming you have latitude and longitude for the destination
        double destinationLatitude = 32.101355;
        double destinationLongitude = 35.202021;

        followMeMissionOperator.startMission(new FollowMeMission(FollowMeHeading.TOWARD_FOLLOW_POSITION,
                destinationLatitude, destinationLongitude, 30f
        ), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Go To mission started successfully");
                } else {
                    showToast("Go To mission start failed: " + djiError.getDescription());
                }
            }
        });
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

            // Start recording
            startRecording();
            recordbtn.setBackgroundResource(R.drawable.ilmstoprecord);
        } else {
            // Stop recording
            stopRecording();
            recordbtn.setBackgroundResource(R.drawable.ilmstartrecord);
        }
    }

    private void startRecording() {
        // Check if the camera is currently recording
        if (isRecording) {
            // Start recording
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
                    moveVideoToInternalStorage();
                } else {
                    showToast("Failed to stop recording: " + djiError.getDescription());
                }
            }
        });
    }
    private void moveVideoToInternalStorage() {
        // Creating an internal storage directory
        File internalStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "ILM-DroneVideos");
        if (!internalStorageDir.exists()) {
            internalStorageDir.mkdirs();
        }

        // Creating a destination file path
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String destinationPath = internalStorageDir + "/video_" + timeStamp + "/";

        // Delay for 2 seconds (adjust the delay time as needed)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Call the method to download the video after the delay
                downloadVideo();

                //showToast("Video saved to internal storage: " + destinationPath);
            }
        }, 5000);
    }
    private void downloadVideo() {
        Camera camera = DJISampleApplication.getAircraftInstance().getCamera();
        if (camera != null) {
            showToast("camera successful");
            MediaManager mediaManager = camera.getMediaManager();
            if (mediaManager != null) {
                showToast("media manager successful");
                List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                showToast("mediaFiles size: " + mediaFiles.size());

//                mediaFiles = mediaManager.getInternalStorageFileListSnapshot();
//                showToast("mediaFiles size: " + mediaFiles.size());
                if (!mediaFiles.isEmpty()) {
                    showToast("mediaFiles not empty");
                    MediaFile mediaFile = mediaFiles.get(mediaManager.getSDCardFileListSnapshot().size() - 1);
                    File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)+"\"ILM-DroneVideos\"", "ILMDownloadedVideo.mp4");
                    mediaFile.fetchFileData(destinationFile, null, new DownloadListener<String>() {
                        @Override
                        public void onStart() {
                            showToast("Started downloading :)");
                        }
                        @Override
                        public void onRateUpdate(long l, long l1, long l2) {
                        }
                        @Override
                        public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {
                        }
                        @Override
                        public void onProgress(long l, long l1) {
                        }
                        @Override
                        public void onSuccess(String s) {
                            showToast("Download successful");
                        }
                        @Override
                        public void onFailure(DJIError djiError) {
                            showToast("Download failed: " + djiError.getDescription());
                        }
                    });
                      }
            }
            else {
                showToast("Download failed:1 ");
            }
        }
        else {
            showToast("Download failed: 2");
        }
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

    public void RepeatRoute() {

    }
}

