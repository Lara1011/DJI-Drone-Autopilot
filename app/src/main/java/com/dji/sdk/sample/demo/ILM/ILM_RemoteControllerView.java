package com.dji.sdk.sample.demo.ILM;

import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;

import com.dji.sdk.sample.R;

import org.osmdroid.views.MapView;

import android.app.Service;
import android.graphics.SurfaceTexture;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;

import com.dji.sdk.sample.internal.utils.VideoFeedView;
import com.dji.sdk.sample.internal.view.PresentableView;

import dji.sdk.codec.DJICodecManager;

public class ILM_RemoteControllerView extends RelativeLayout implements View.OnClickListener, PresentableView, TextureView.SurfaceTextureListener {
    private MapView mapView;
    private Context context;
    private ILM_MapController mapController;
    private ILM_StatusBar statusBar;
    private ILM_CSVLog csvLog;
    private ILM_Buttons buttons;

    private ILM_VirtualStickView virtualStickView;

    private TextView battery, speed, x, y, z, pitch, roll, yaw, date, distance, latitude, longitude, altitude;
    private ILM_Missions missions;
    private ILM_Waypoints waypoints;
    private ILM_AllWaypoints allWaypoints;

    private TextView peopleDetected;
    private ImageView image;
    private VideoFeedView videoFeedView;
    private TextureView videoSurface;
    private View view;
    private ILM_Video video;
    private DJICodecManager mCodecManager = null;
    private boolean isExpanded = false;

    public ILM_RemoteControllerView(Context context) {
        super(context);
        this.context = context;
        init();
        video = new ILM_Video(videoFeedView, image, videoSurface, peopleDetected);
    }

    private void init() {
        setClickable(true);
        //<<=====================Status Bar View==========================>>//
        statusBar = new ILM_StatusBar();
        //<<==========================Virtual Stick=========================>>//
        virtualStickView = new ILM_VirtualStickView(context);
        addView(virtualStickView);
        virtualStickView.setVisibility(View.GONE);
        virtualStickView.setClickable(false);
        //<<==============================================================>>//
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_ilm_remote_controller, this, true);
        initUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {
        //<<=========================CSV Log==========================>>//
        csvLog = new ILM_CSVLog(context, statusBar);
        csvLog.createLogBrain();
        //<<==========================Map==========================>>//
        mapView = findViewById(R.id.mapView_ILM);
        mapController = new ILM_MapController(context, mapView);
        mapController.init();
        //<<==========================Video==========================>>//
        peopleDetected = findViewById(R.id.textView_ILM_PeopleDetected);
        videoFeedView = findViewById(R.id.videoFeedView_ILM);
        videoSurface = findViewById(R.id.video_previewer_surface);
        image = findViewById(R.id.imageView_ILM_Image);
        view = findViewById(R.id.view_ILM_coverView);

        resizeVideo();


        if (videoSurface != null) {
            videoSurface.setSurfaceTextureListener(this);
        }
        //<<==========================Status Bar==========================>>//
        latitude = findViewById(R.id.textView_ILM_LatitudeInt1);
        longitude = findViewById(R.id.textView_ILM_LongitudeInt1);
        altitude = findViewById(R.id.textView_ILM_AltitudeInt1);

        x = findViewById(R.id.textView_ILM_XInt1);
        y = findViewById(R.id.textView_ILM_YInt1);
        z = findViewById(R.id.textView_ILM_ZInt1);

        speed = findViewById(R.id.textView_ILM_SpeedInt1);
        distance = findViewById(R.id.textView_ILM_DistanceInt1);
        battery = findViewById(R.id.textView_ILM_BatteryInt1);
        date = findViewById(R.id.textView_ILM_DateInt1);

        pitch = findViewById(R.id.textView_ILM_PitchInt1);
        roll = findViewById(R.id.textView_ILM_RollInt1);
        yaw = findViewById(R.id.textView_ILM_YawInt1);
        //<<==========================Status Bar Updates==========================>>//
        statusBar.updateDateTime(date);
        statusBar.updateBattery(battery);
        statusBar.updateSpeed(speed);
        statusBar.updateXYZ(x, y, z);
        statusBar.updateLatitudeLongitudeAltitude(latitude, longitude, altitude);
        statusBar.updatePitchRollYaw(pitch, roll, yaw);

        //<<==========================Waypoints==========================>>//<
        waypoints = new ILM_Waypoints(context, statusBar);
        waypoints.createLogBrain();

        allWaypoints = new ILM_AllWaypoints(context, statusBar);
        //<<==========================Buttons==========================>>//
        buttons = new ILM_Buttons(context, this);

        buttons.peopleDetectionBtn.setOnClickListener(this);

        buttons.returnToHomeBtn.setOnClickListener(this);

        buttons.takeOffBtn.setOnClickListener(this);
        buttons.stopBtn.setOnClickListener(this);
        buttons.landBtn.setOnClickListener(this);
        buttons.goToBtn.setOnClickListener(this);
        buttons.enableVirtualStickBtn.setOnClickListener(this);
//        buttons.panicStopBtn.setOnClickListener(this);
        buttons.recordBtn.setOnClickListener(this);
        buttons.waypointBtn.setOnClickListener(this);

        buttons.cameraAdjustBtn.setOnClickListener(this);
        buttons.adjustPitchPlusBtn.setOnClickListener(this);
        buttons.adjustPitchMinusBtn.setOnClickListener(this);
        buttons.adjustRollPlusBtn.setOnClickListener(this);
        buttons.adjustRollMinusBtn.setOnClickListener(this);
        buttons.adjustYawPlusBtn.setOnClickListener(this);
        buttons.adjustYawMinusBtn.setOnClickListener(this);

        buttons.addWaypointBtn.setOnClickListener(this);
        buttons.removeWaypointBtn.setOnClickListener(this);

        buttons.repeatRouteBtn.setOnClickListener(this);

        buttons.missionsBtn.setOnClickListener(this);
        buttons.mission1Btn.setOnClickListener(this);
        buttons.mission2Btn.setOnClickListener(this);
        buttons.mission3Btn.setOnClickListener(this);

        buttons.waypointsBtn.setOnClickListener(this);
        buttons.waypoint1Btn.setOnClickListener(this);
        buttons.waypoint2Btn.setOnClickListener(this);
        buttons.waypoint3Btn.setOnClickListener(this);
        buttons.waypoint4Btn.setOnClickListener(this);
        buttons.waypoint5Btn.setOnClickListener(this);
        buttons.waypoint6Btn.setOnClickListener(this);
        buttons.waypoint7Btn.setOnClickListener(this);
        buttons.waypoint8Btn.setOnClickListener(this);
        videoFeedView.setCoverView(view);

        buttons.mapResizeBtn.setOnClickListener(this);

        missions = new ILM_Missions(getContext(), statusBar, mapController);
        addMissions();

    }

    private void addMissions() {
        for (int i = 0; i < 3; i++) {
            missions.addMission();
        }
    }

    public void switchToVirtualStickLayout() {
        //findViewById(R.id.buttons_relativeLayout).setClickable(false);
        findViewById(R.id.buttons_relativeLayout).setVisibility(View.INVISIBLE);
        virtualStickView = new ILM_VirtualStickView(context);
        addView(virtualStickView);
        buttons.EnableVirtualStick();
    }

    public void switchToMainLayout() {
        removeView(virtualStickView);

        //findViewById(R.id.buttons_relativeLayout).setClickable(true);
        findViewById(R.id.buttons_relativeLayout).setVisibility(View.VISIBLE);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ILM_Take_Off:
                buttons.takeOff();
                break;
            case R.id.btn_ILM_Stop:
                buttons.stop();
                break;
            case R.id.btn_ILM_Land:
                buttons.land();
                break;
            case R.id.btn_ILM_GoTo:
                buttons.goTo(waypoints, mapController);
                break;
            case R.id.btn_ILM_RepeatRoute:
                buttons.RepeatRoute(waypoints, mapController);
                break;
            case R.id.btn_ILM_Enable_VirtualStick:
                switchToVirtualStickLayout();
                break;
//            case R.id.btn_ILM_Panic_Stop:
//                buttons.panicStop();
//                break;
            case R.id.btn_ILM_Record:
                buttons.isRecording = !buttons.isRecording;
                buttons.record();
                break;
            case R.id.btn_ILM_Waypoint:
                buttons.waypointBtn();
                break;
            case R.id.btn_ILM_AddWaypoint:
                buttons.addWaypoint(waypoints, mapController, allWaypoints);
                break;
            case R.id.btn_ILM_RemoveWaypoint:
                buttons.removeWaypoint(waypoints, mapController);
                break;
            case R.id.btn_ILM_CameraAdjust:
                buttons.cameraAdjustVisibility();
                break;
            case R.id.btn_ILM_AdjustPitchPlus:
                buttons.cameraAdjust("pitch", '+');
                Log.e("AdjustPitchPlus", "AdjustPitchPlus");
                break;
            case R.id.btn_ILM_AdjustPitchMinus:
                buttons.cameraAdjust("pitch", '-');
                Log.e("AdjustPitchMinus", "AdjustPitchMinus");
                break;
            case R.id.btn_ILM_AdjustRollPlus:
                buttons.cameraAdjust("roll", '+');
                Log.e("AdjustRollPlus", "AdjustRollPlus");
                break;
            case R.id.btn_ILM_AdjustRollMinus:
                buttons.cameraAdjust("roll", '-');
                Log.e("AdjustRollMinus", "AdjustRollMinus");
                break;
            case R.id.btn_ILM_AdjustYawPlus:
                buttons.cameraAdjust("yaw", '+');
                Log.e("AdjustYawPlus", "AdjustYawPlus");
                break;
            case R.id.btn_ILM_AdjustYawMinus:
                buttons.cameraAdjust("yaw", '-');
                Log.e("AdjustYawMinus", "AdjustYawMinus");
                break;
            case R.id.btn_ILM_ReturnToHome:
                buttons.returnToHome();
                break;
            case R.id.btn_ILM_Missions:
                buttons.missionListBtn();
                break;
            case R.id.btn_ILM_Waypoints:
                buttons.waypointsBtn();
                break;
            case R.id.btn_ILM_Mission_1:
                buttons.RepeatRoute(missions.loadMission(1), mapController);
                break;
            case R.id.btn_ILM_Mission_2:
                buttons.RepeatRoute(missions.loadMission(2), mapController);
                break;
            case R.id.btn_ILM_Mission_3:
                buttons.RepeatRoute(missions.loadMission(3), mapController);
                break;
            case R.id.btn_ILM_Waypoint_1:
                buttons.setCounter = buttons.count;
                buttons.count = 0;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_2:
                buttons.setCounter = buttons.count;
                buttons.count = 1;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_3:
                buttons.setCounter = buttons.count;
                buttons.count = 2;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_4:
                buttons.setCounter = buttons.count;
                buttons.count = 3;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_5:
                buttons.setCounter = buttons.count;
                buttons.count = 4;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_6:
                buttons.setCounter = buttons.count;
                buttons.count = 5;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_7:
                buttons.setCounter = buttons.count;
                buttons.count = 6;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_Waypoint_8:
                buttons.setCounter = buttons.count;
                buttons.count = 7;
                buttons.goTo(allWaypoints, mapController);
                break;
            case R.id.btn_ILM_MapResize:
                Log.e("mapView_ILM", "mapView_ILM");
                buttons.mapResize(isExpanded, mapView);
                isExpanded = !isExpanded;
                break;
            case R.id.btn_ILM_PeopleDetection:
                video.setDetectionEnabled(!video.isDetectionEnabled());
                Log.e("processFrame", "Detection enabled: " + video.isDetectionEnabled());
                break;
            default:
                break;
        }
    }

    @Override
    public int getDescription() {
        return R.string.component_listview_ilm_remote_controller;
    }

    @NonNull
    @Override
    public String getHint() {
        return this.getClass().getSimpleName() + ".java";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshView();
        video.displayVideo();
        DJISampleApplication.getEventBus().post(new MainActivity.RequestStartFullScreenEvent());

    }

    // Refresh the view
    public void refreshView() {
        invalidate(); // Invalidate the view, forcing a redraw
    }

    @Override
    protected void onDetachedFromWindow() {
        if (csvLog != null)
            csvLog.closeLogBrain();     //Closing CSV
        waypoints.closeLogBrain();
        DJISampleApplication.getEventBus().post(new MainActivity.RequestEndFullScreenEvent());
        mapController.stopLocationUpdates();
        super.onDetachedFromWindow();
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            showToast(width + " " + height);
            mCodecManager = new DJICodecManager(context, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
    }

    private void resizeVideo() {
        // Get the screen height
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        // Convert 30dp to pixels
        int dpToPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        // Calculate the new height
        int newHeight = screenHeight - dpToPx;

        // Apply the height to all the views
        setViewHeight(R.id.relativeLayout, screenHeight);
        setViewHeight(R.id.videoFeedView_ILM, screenHeight);
        setViewHeight(R.id.video_previewer_surface, screenHeight);
        setViewHeight(R.id.imageView_ILM_Image, screenHeight);
        setViewHeight(R.id.view_ILM_coverView, screenHeight);
    }

    private void setViewHeight(int viewId, int height) {
        View view = findViewById(viewId);
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

}


