package com.dji.sdk.sample.demo.ILM;
import java.util.Date;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;
import static com.google.android.gms.internal.zzahn.runOnUiThread;

import android.app.Service;
import android.content.Context;
import android.graphics.SurfaceTexture;
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

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.ModuleVerificationUtil;
import com.dji.sdk.sample.internal.utils.ToastUtils;
import com.dji.sdk.sample.internal.utils.VideoFeedView;
import com.dji.sdk.sample.internal.view.PresentableView;

import dji.common.airlink.PhysicalSource;
import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.callback.SetCallback;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.sdkmanager.DJISDKManager;

import org.osmdroid.views.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ILMRemoteControllerView extends RelativeLayout
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener,
        PresentableView, TextureView.SurfaceTextureListener{

    private Context ctx;
    private Button GoTobtn;
    private Button Stopbtn;
    private Button Landbtn;
    private MapView mapView = null;
    private TextView Battery;
    private TextView x;
    private TextView y;
    private TextView z;
    private TextView latitude;
    private TextView longtitude;
    private TextView altitude;
    private TextView Date;
    //private TextView Time;
    private TextView Speed;
    private TextView Distance;
    private TextView Pitch;
    private TextView Roll;
    private TextView Yaw;
    private VideoFeedView videoFeedView;
    private View view;
    private ILMController controller;
    protected DJICodecManager mCodecManager = null;
    private SetCallback setBandwidthCallback;
    private VideoFeeder.PhysicalSourceListener sourceListener;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Handler handler = new Handler();
    private ILMMapController mapController;
    private ILMVideoController videoController;

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

        x = (TextView) findViewById(R.id.textView_ILM_XInt);
        y = (TextView) findViewById(R.id.textView_ILM_YInt);
        z = (TextView) findViewById(R.id.textView_ILM_ZInt);
        latitude = (TextView) findViewById(R.id.textView_ILM_Latitude);
        longtitude = (TextView) findViewById(R.id.textView_ILM_Longitude);
        altitude = (TextView) findViewById(R.id.textView_ILM_Altitude);

        Speed = (TextView) findViewById(R.id.textView_ILM_SpeedInt);
        Distance = (TextView) findViewById(R.id.textView_ILM_DistanceInt);
        Battery = (TextView) findViewById(R.id.textView_ILM_BatteryInt);
        Date = (TextView) findViewById(R.id.textView_ILM_DateInt);

        updateDateTime();

        Pitch = (TextView) findViewById(R.id.textView_ILM_PitchInt);
        Roll = (TextView) findViewById(R.id.textView_ILM_RollInt);
        Yaw = (TextView) findViewById(R.id.textView_ILM_YawInt);

        mapView = (MapView) findViewById(R.id.mapView_ILM);
        mapController = new ILMMapController(ctx, mapView);
        videoFeedView = (VideoFeedView) findViewById(R.id.videoFeedView_ILM);
        view = (View) findViewById(R.id.view_ILM_coverView);

        Stopbtn.setOnClickListener(this);
        Landbtn.setOnClickListener(this);
        GoTobtn.setOnClickListener(this);
        videoFeedView.setCoverView(view);
    }

    @Override
    public void onClick(View v) {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        switch (v.getId()) {
            case R.id.btn_ILM_Stop:
                flightController.getFlightAssistant().setLandingProtectionEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) showToast("" + djiError);
                        else showToast("Landing protection DISABLED!");
                    }
                });
                disable(flightController);
            case R.id.btn_ILM_GoTo:
            case R.id.btn_ILM_Land:
        }
    }

    private void disable(FlightController flightController) {
//        autonomous_mode_txt.setText("manual");
//        autonomous_mode_txt.setTextColor(Color.rgb(255,0,0));


        //send stop to aircraft
        controller.stopOnPlace();

        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError == null) {
                    ToastUtils.setResultToToast("Virtual sticks disabled!");
                }
            }
        });
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

    private void updateGimbalState() {
        if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
            Gimbal gimbal = DJISampleApplication.getProductInstance().getGimbal();
            if (gimbal != null) {
                gimbal.setStateCallback(gimbalState -> {
                    runOnUiThread(() -> {
                        Pitch.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getPitch()));
                        Roll.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getRoll()));
                        Yaw.setText(String.valueOf((int) gimbalState.getAttitudeInDegrees().getYaw()));
                    });
                });
            }
        }
    }


    private void updateDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault());

        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                String formattedDateTime = dateFormat.format(new Date());

                if (Date != null) {
                    Date.setText(formattedDateTime);
                }
                handler.postDelayed(this, 1000);
            }
        };
        updateTimeRunnable.run();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DJISampleApplication.getEventBus().post(new MainActivity.RequestStartFullScreenEvent());

        videoController.displayVideo();
        updateGimbalState();
    }


    @Override
    protected void onDetachedFromWindow() {
        DJISampleApplication.getEventBus().post(new MainActivity.RequestEndFullScreenEvent());
        super.onDetachedFromWindow();
    }
}
