package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.dji.sdk.sample.R;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.util.ArrayList;
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
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.followme.FollowMeMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.MissionControl;

public class ILMButtons {
    protected Button goTobtn;
    protected Button stopbtn;
    protected Button landbtn;
    protected Button takeOffbtn;
    private Context context;
    FlightController flightController = ModuleVerificationUtil.getFlightController();
//    private double lat;
//    private double lon;
//    private float alt;


    public ILMButtons(Context context, View view) {
        goTobtn = view.findViewById(R.id.btn_ILM_GoTo);
        stopbtn = view.findViewById(R.id.btn_ILM_Stop);
        landbtn = view.findViewById(R.id.btn_ILM_Land);
        takeOffbtn = view.findViewById(R.id.btn_ILM_Take_Off);
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
        flightController.getFlightAssistant().setLandingProtectionEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast(context.getResources().getString(R.string.success));
                    disable(flightController);
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
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
}

