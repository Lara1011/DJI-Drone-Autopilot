package com.dji.sdk.sample.demo.ILM;

import static com.dji.sdk.sample.internal.utils.ToastUtils.showToast;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.dji.sdk.sample.internal.utils.ToastUtils;

public class ILM_FollowMe {
    private ILM_GoTo goTo;
    private ILM_GPS gps;
    private final double ALTITUDE = 20.0;
    private final double SPEED = 2.0;
    private final double RADIUS = 30.0;
    private final double MINIMUM_DISTANCE = 3.0;
    protected boolean isFollowMe = false;
    private Handler handler = new Handler();
    private Runnable followMeRunnable;

    public ILM_FollowMe(Context context) {
        gps = new ILM_GPS(context);
        gps.requestLocationUpdates();
        goTo = new ILM_GoTo(gps.getLatitude(), gps.getLongitude(), ALTITUDE, SPEED, RADIUS, MINIMUM_DISTANCE);
    }

    protected void FollowMe() {
        isFollowMe = !isFollowMe;
        if (!isFollowMe) {
            stopFollowing();
            return;
        }

        goTo.setMode(1);
        goTo.isGoTo = true;
        followMeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFollowMe) {
                    stopFollowing();
                    return;
                }
                goTo.setWaypoint(gps.getLatitude(), gps.getLongitude(), ALTITUDE);
                goTo.goTo();
                // Check if the target position differs from the current one
                if (goTo.getLat() != gps.getLatitude() || goTo.getLon() != gps.getLongitude()) {
                    goTo.setWaypoint(gps.getLatitude(), gps.getLongitude(), ALTITUDE);
                    goTo.goTo();
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(followMeRunnable);
    }

    protected void stopFollowing() {
        isFollowMe = false;
        if (handler != null && followMeRunnable != null) {
            handler.removeCallbacks(followMeRunnable);
            goTo.disableGoTo();
            //gps.onDestroy();
        }
    }
}
