package com.vaavud.vaavudSDK.core;

import android.content.Context;

import com.vaavud.vaavudSDK.core.listener.HeadingListener;
import com.vaavud.vaavudSDK.core.listener.OrientationListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.mjolnir.MjolnirController;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.orientation.OrientationController;
import com.vaavud.vaavudSDK.core.orientation.SensorFusion;
import com.vaavud.vaavudSDK.core.sleipnir.SleipnirController;
import com.vaavud.vaavudSDK.core.sleipnir.listener.AnalysisListener;

/**
 * Created by aokholm on 15/01/16.
 */
public class VaavudCoreSDK implements SpeedListener, HeadingListener {

    Context context;

    private SpeedListener speedListener;
    private StatusListener statusListener;
    private AnalysisListener analysisListener;
    private OrientationListener orientationListener;
    private HeadingListener headingListener;

    private SleipnirController _sleipnir;
    private MjolnirController _mjolnir;
    private SensorFusion _orientation;

    private boolean sleipnirActive;

    public VaavudCoreSDK(Context context) {
        this.context = context;
    }

    public void startMjolnir() throws VaavudError {
        orientation().start();
        mjolnir().setSpeedListener(this);
        mjolnir().start();
    }

    public void stopMjolnir() {
        mjolnir().stop();
    }

    public void startSleipnir() throws VaavudError {
        orientation().setHeadingListener(this);

        sleipnir().setSpeedListener(this);
        sleipnir().setAnalysisListener(analysisListener);

        orientation().start();
        sleipnir().start();
    }

    public void stopSleipnir() {
        sleipnir().stop();
        orientation().stop();
    }

    private MjolnirController mjolnir() {
        if (_mjolnir == null) {
            _mjolnir = new MjolnirController(context, new OrientationController(context)); // FIXME: 19/01/16 Mjolnir controller should use sensor manager
        }
        return _mjolnir;
    }

    private SleipnirController sleipnir() {
        if (_sleipnir == null) {
            _sleipnir = new SleipnirController(context);
        }
        return _sleipnir;
    }

    private SensorFusion orientation() {
        if (_orientation == null) {
            _orientation = new SensorFusion(context);
        }
        return _orientation;
    }

    public void setSpeedListener(SpeedListener speedListener) {
        this.speedListener = speedListener;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    // debug
    public void setAnalysisListener(AnalysisListener analysisListener) {
        this.analysisListener = analysisListener;
    }

    public void setHeadingListener(HeadingListener headingListener) {
        this.headingListener = headingListener;
    }

    public void setOrientationListener(OrientationListener orientationListener) {
        orientation().setOrientationListener(orientationListener);
    }

    public boolean isSleipnirActive() {
        return sleipnir().isActive();
    }

    @Override
    public void speedChanged(SpeedEvent event) {
        this.speedListener.speedChanged(event);
    }

    @Override
    public void newHeading(float heading) {
        if (headingListener != null) headingListener.newHeading(heading);
        sleipnir().newHeading(heading);
    }
}
