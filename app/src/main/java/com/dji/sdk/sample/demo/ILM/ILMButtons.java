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


    public ILMButtons(Context context, View view) {
        goTobtn = view.findViewById(R.id.btn_ILM_GoTo);
        stopbtn = view.findViewById(R.id.btn_ILM_Stop);
        landbtn = view.findViewById(R.id.btn_ILM_Land);
        takeOffbtn = view.findViewById(R.id.btn_ILM_Take_Off);
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

    }

    private void stopOnPlace() {

    }

    public void disable(FlightController flightController) {

    }
}

