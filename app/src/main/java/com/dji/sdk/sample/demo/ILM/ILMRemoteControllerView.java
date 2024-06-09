package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.app.Service;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.VideoFeedView;
import com.dji.sdk.sample.internal.view.PresentableView;

import dji.sdk.codec.DJICodecManager;

import org.osmdroid.views.MapView;


public class ILMRemoteControllerView extends RelativeLayout
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener,
        PresentableView, TextureView.SurfaceTextureListener {

    private Context ctx;
    private Button GoTobtn;
    private Button Stopbtn;
    private Button Landbtn;
    private Button TakeOffbtn;
    private Button Recordbtn;
    private Button Waypointbtn;
    private Button AddWaypointbtn;
    private Button RepeatRoutebtn;
    private Button ChangeWaypointbtn;
    private Button CameraControlbtn;
    private Button CameraYawUp;
    private Button CameraYawDown;
    private Button CameraRollUp;
    private Button CameraRollDown;
    private Button CameraPitchUp;
    private Button CameraPitchDown;
    private Button LoadCSV;
    private Button RemoveWaypoint;
    private MapView mapView = null;
    private TextView Battery;
    private TextView x;
    private TextView y;
    private TextView z;
    private TextView latitude;
    private TextView longitude;
    private TextView altitude;
    private TextView DateTime;
    private TextView Speed;
    private TextView Distance;
    private TextView Pitch;
    private TextView Roll;
    private TextView Yaw;
    private VideoFeedView videoFeedView;
    private View view;
    protected DJICodecManager mCodecManager = null;
    private ILMMapController mapController;
    private ILMVideoController videoController;
    private ILMInfoUpdate infoUpdate;
    private ILMCSVLog ilmLog;
    private ILMWaypoints ilmWaypoints;
    private ILMButtons buttons;
    private RelativeLayout waypointButtonsLayout;
    private LinearLayout cameraControlsLayout;
    private ILMUploadWaypoints uploadWaypoints;

    public ILMRemoteControllerView(Context context) {
        super(context);
        ctx = context;
        init(context);
        videoController = new ILMVideoController(videoFeedView);
    }

    private void init(Context context) {
        setClickable(true);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_ilm_rc, this, true);
        initUI();
        mapController.init(ctx,mapView);
    }

    private void initUI() {
        Stopbtn = (Button) findViewById(R.id.btn_ILM_Stop);
        Landbtn = (Button) findViewById(R.id.btn_ILM_Land);
        GoTobtn = (Button) findViewById(R.id.btn_ILM_GoTo);
        TakeOffbtn = (Button) findViewById(R.id.btn_ILM_Take_Off);

        Recordbtn = (Button) findViewById(R.id.btn_ILM_Record);

        Waypointbtn = (Button) findViewById(R.id.btn_ILM_Waypoint);
        AddWaypointbtn = (Button) findViewById(R.id.btn_ILM_Add_Waypoint);
        RepeatRoutebtn = (Button) findViewById(R.id.btn_ILM_Repeat_Route);
        ChangeWaypointbtn = (Button) findViewById(R.id.btn_ILM_Change_Waypoint);
        RemoveWaypoint = (Button) findViewById(R.id.btn_ILM_Remove_Waypoint);

        CameraControlbtn = (Button) findViewById(R.id.btn_ILM_camera_controls);
        CameraYawUp = (Button) findViewById(R.id.btn_ILM_camera_yaw_up);
        CameraYawDown = (Button) findViewById(R.id.btn_ILM_camera_yaw_down);
        CameraRollUp = (Button) findViewById(R.id.btn_ILM_camera_roll_up);
        CameraRollDown = (Button) findViewById(R.id.btn_ILM_camera_roll_down);
        CameraPitchUp = (Button) findViewById(R.id.btn_ILM_camera_pitch_up);
        CameraPitchDown = (Button) findViewById(R.id.btn_ILM_camera_pitch_down);

        LoadCSV = (Button) findViewById(R.id.btn_ILM_LoadCSV);


        x = (TextView) findViewById(R.id.textView_ILM_XInt);
        y = (TextView) findViewById(R.id.textView_ILM_YInt);
        z = (TextView) findViewById(R.id.textView_ILM_ZInt);
        latitude = (TextView) findViewById(R.id.textView_ILM_Latitude);
        longitude = (TextView) findViewById(R.id.textView_ILM_Longitude);
        altitude = (TextView) findViewById(R.id.textView_ILM_Altitude);

        Speed = (TextView) findViewById(R.id.textView_ILM_SpeedInt);
        Distance = (TextView) findViewById(R.id.textView_ILM_DistanceInt);
        Battery = (TextView) findViewById(R.id.textView_ILM_BatteryInt);
        DateTime = (TextView) findViewById(R.id.textView_ILM_DateInt);

        Pitch = (TextView) findViewById(R.id.textView_ILM_PitchInt);
        Roll = (TextView) findViewById(R.id.textView_ILM_RollInt);
        Yaw = (TextView) findViewById(R.id.textView_ILM_YawInt);

        mapView = (MapView) findViewById(R.id.mapView_ILM);
        mapController = new ILMMapController(ctx, mapView);
        videoFeedView = (VideoFeedView) findViewById(R.id.videoFeedView_ILM);
        view = (View) findViewById(R.id.view_ILM_coverView);

        waypointButtonsLayout = (RelativeLayout) findViewById(R.id.waypointButtonsLayout);
        cameraControlsLayout = (LinearLayout) findViewById(R.id.cameraControlsLayout);

        infoUpdate = new ILMInfoUpdate(Battery,x,y,z,latitude, longitude,altitude,DateTime,Speed,Distance,Pitch,Roll,Yaw);
        infoUpdate.updateDateTime();

        ilmLog = new ILMCSVLog(ctx, infoUpdate);
        ilmLog.createLogBrain();
        ilmWaypoints = new ILMWaypoints(ctx, infoUpdate);
        ilmWaypoints.createLogBrain();
        buttons = new ILMButtons(ctx, this);
        buttons.takeOffbtn.setOnClickListener(this);
        buttons.stopbtn.setOnClickListener(this);
        buttons.landbtn.setOnClickListener(this);
        buttons.goTobtn.setOnClickListener(this);
        buttons.recordbtn.setOnClickListener(this);
        buttons.Waypointbtn.setOnClickListener(this);
        buttons.AddWaypointbtn.setOnClickListener(this);
        buttons.RepeatRoutebtn.setOnClickListener(this);
        buttons.ChangeWaypointbtn.setOnClickListener(this);
        buttons.RemoveWaypoint.setOnClickListener(this);
        buttons.CameraControlbtn.setOnClickListener(this);
        buttons.CameraYawUp.setOnClickListener(this);
        buttons.CameraYawDown.setOnClickListener(this);
        buttons.CameraRollUp.setOnClickListener(this);
        buttons.CameraRollDown.setOnClickListener(this);
        buttons.CameraPitchUp.setOnClickListener(this);
        buttons.CameraPitchDown.setOnClickListener(this);
        buttons.LoadCSV.setOnClickListener(this);

        uploadWaypoints = new ILMUploadWaypoints(getContext());

        videoFeedView.setCoverView(view);
    }

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
                buttons.goTo(ilmWaypoints);
                break;
            case R.id.btn_ILM_Record:
                buttons.isRecording = !buttons.isRecording;
                buttons.record();
                break;
            case R.id.btn_ILM_Waypoint:
                buttons.WaypointsList();
                break;
            case R.id.btn_ILM_Add_Waypoint:
                buttons.AddWaypoint(ilmWaypoints, mapController);
                //mapController.addWaypoint();
                break;
            case R.id.btn_ILM_Repeat_Route:
                buttons.RepeatRoute(ilmWaypoints);
                break;
            case R.id.btn_ILM_Change_Waypoint:
                buttons.ChangeWaypoint(ilmWaypoints);
                break;
            case R.id.btn_ILM_camera_controls:
                if (cameraControlsLayout.getVisibility() == View.VISIBLE) {
                    cameraControlsLayout.setVisibility(View.GONE);
                } else {
                    cameraControlsLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_ILM_camera_yaw_up:
                buttons.cameraControl("yaw",'+');
                break;
            case R.id.btn_ILM_camera_yaw_down:
                buttons.cameraControl("yaw",'-');
                break;
            case R.id.btn_ILM_camera_roll_up:
                buttons.cameraControl("roll",'+');
                break;
            case R.id.btn_ILM_camera_roll_down:
                buttons.cameraControl("roll",'-');
                break;
            case R.id.btn_ILM_camera_pitch_up:
                buttons.cameraControl("pitch",'+');
                break;
            case R.id.btn_ILM_camera_pitch_down:
                buttons.cameraControl("pitch",'-');
                break;
            case R.id.btn_ILM_LoadCSV:
                buttons.uploadRoute(ilmWaypoints, uploadWaypoints, mapController);
                break;
            case R.id.btn_ILM_Remove_Waypoint:
                buttons.removeWaypoint(ilmWaypoints, mapController);
                break;
        }
    }


    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.e("ILM", "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            showToast("" + width + "," + height);
            mCodecManager = new DJICodecManager(ctx, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        Log.e("ILM", "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        Log.e("ILM", "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

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
        DJISampleApplication.getEventBus().post(new MainActivity.RequestStartFullScreenEvent());

        videoController.displayVideo();
        infoUpdate.updateGimbalState();
        infoUpdate.updateBatteryPercentage();
        infoUpdate.updateLatitudeLongitude();
        infoUpdate.updateXYZ();
        infoUpdate.updateSpeed();
    }


    @Override
    protected void onDetachedFromWindow() {
        DJISampleApplication.getEventBus().post(new MainActivity.RequestEndFullScreenEvent());
        ilmLog.closeLogBrain();
        ilmWaypoints.closeLogBrain();
        mapController.stopLocationUpdates();
        super.onDetachedFromWindow();
    }
}
