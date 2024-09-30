package com.dji.sdk.sample.demo.ILM;

import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.OnScreenJoystickListener;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.OnScreenJoystick;
import com.dji.sdk.sample.internal.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

public class ILM_VirtualStickView extends RelativeLayout implements View.OnClickListener {
    private Button btnDisableVirtualStick;
    private OnScreenJoystick screenJoystickRight;
    private OnScreenJoystick screenJoystickLeft;
    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;
    private float pitch;        // Forward and backward
    private float roll;         // Side to Side movement
    private float yaw;          // Rotation of the drone (angle change)
    private float throttle;     // Up and Down movement
    private FlightController flightController = null;
    private boolean flag = false;

    public ILM_VirtualStickView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DJISampleApplication.getEventBus().post(new MainActivity.RequestStartFullScreenEvent());
        setUpListeners();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (null != sendVirtualStickDataTimer) {
            if (sendVirtualStickDataTask != null) {
                sendVirtualStickDataTask.cancel();
            }
            sendVirtualStickDataTimer.cancel();
            sendVirtualStickDataTimer.purge();
            sendVirtualStickDataTimer = null;
            sendVirtualStickDataTask = null;
        }
        tearDownListeners();
        DJISampleApplication.getEventBus().post(new MainActivity.RequestStartFullScreenEvent());
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_ilm_virtual_stick, this, true);
        initParams();
        initUI();
    }

    private void initParams() {
        if (flightController == null) {
            if (ModuleVerificationUtil.isFlightControllerAvailable()) {
                flightController = DJISampleApplication.getAircraftInstance().getFlightController();
            }
        }
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
    }

    private void initUI() {
        screenJoystickRight = findViewById(R.id.directionJoystickRight);
        screenJoystickLeft = findViewById(R.id.directionJoystickLeft);
        btnDisableVirtualStick = findViewById(R.id.button_ILM_disableVirtualStick);

        btnDisableVirtualStick.setOnClickListener(this);
    }

    private void setUpListeners() {
        if (flag) {
            return;
        }
        screenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

                pitch = pitchJoyControlMaxSpeed * pY;
                roll = rollJoyControlMaxSpeed * pX;
//                Log.d("TAG", "pitch: " + pitch + " roll: " + roll);

                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
                }
            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 4;
                float yawJoyControlMaxSpeed = 20;

                yaw = yawJoyControlMaxSpeed * pX;
                throttle = verticalJoyControlMaxSpeed * pY;
                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
                }
            }
        });
    }

    private void tearDownListeners() {
        screenJoystickLeft.setJoystickListener(null);
        screenJoystickRight.setJoystickListener(null);
    }

    @Override
    public void onClick(View v) {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        if (v.getId() == R.id.button_ILM_disableVirtualStick) {
            flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        ToastUtils.setResultToToast(djiError.toString());
//                    DialogUtils.showDialogBasedOnError(getContext(), djiError);
                    }
                }
            });
            ((ILM_RemoteControllerView) getParent()).switchToMainLayout();
        }
        flag = false;
    }


    private class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            if (flightController != null) {
                flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            ToastUtils.setResultToToast(djiError.getDescription());
                        } else {
//                            Log.d("SendVirtualStickDataTask", "roll1: " + roll + " pitch1: " + pitch + " yaw1: " + yaw + " throttle1: " + throttle);
                        }
                    }
                });
            }
        }
    }

}
