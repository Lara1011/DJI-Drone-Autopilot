package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.DialogUtils;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.VideoFeedView;
import com.dji.sdk.sample.internal.view.PresentableView;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.callback.SetCallback;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;

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
    private ILMButtons buttons;

    public ILMRemoteControllerView(Context context) {
        super(context);
        ctx = context;
        init(context);
        videoController = new ILMVideoController(videoFeedView);
        showToast("Log file created");
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

        infoUpdate = new ILMInfoUpdate(Battery,x,y,z,latitude, longitude,altitude,DateTime,Speed,Distance,Pitch,Roll,Yaw);
        infoUpdate.updateDateTime();

        ilmLog = new ILMCSVLog(ctx, infoUpdate);
        ilmLog.createLogBrain();
        buttons = new ILMButtons(ctx, this);
        buttons.takeOffbtn.setOnClickListener(this);
        buttons.stopbtn.setOnClickListener(this);
        buttons.landbtn.setOnClickListener(this);
        buttons.goTobtn.setOnClickListener(this);
        buttons.recordbtn.setOnClickListener(this);
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
                buttons.goTo();
                break;
            case R.id.btn_ILM_Record:
                buttons.isRecording = !buttons.isRecording;
                buttons.record();
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
        super.onDetachedFromWindow();
    }
}
