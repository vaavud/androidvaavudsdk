package com.vaavud.vaavudSDK.core;

import android.content.Context;

import com.vaavud.vaavudSDK.core.listener.DirectionListener;
import com.vaavud.vaavudSDK.core.listener.HeadingListener;
import com.vaavud.vaavudSDK.core.listener.OrientationListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.mjolnir.MjolnirController;
import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.orientation.OrientationController;
import com.vaavud.vaavudSDK.core.orientation.SensorFusion;
import com.vaavud.vaavudSDK.core.sleipnir.SleipnirController;
import com.vaavud.vaavudSDK.core.sleipnir.listener.AnalysisListener;
import com.vaavud.vaavudSDK.core.sleipnir.model.Direction;

/**
 * Created by aokholm on 15/01/16.
 */
public class VaavudCoreSDK implements SpeedListener, DirectionListener, HeadingListener {

    Context context;

    private SpeedListener speedListener;
    private DirectionListener directionListener;
    private StatusListener statusListener;
    private AnalysisListener analysisListener;
    private OrientationListener orientationListener;
    private HeadingListener headingListener;
    private DirectionListener directionListener;

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
        sleipnir().setDirectionListener(this);

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

    public SleipnirController sleipnir() { // for debugging
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

    public void setSpeedListener(SpeedListener _speedListener) {
        speedListener = _speedListener;
    }

    public void setStatusListener(StatusListener _statusListener) {
        statusListener = _statusListener;
    }

    public void setDirectionListener(DirectionListener _directionListener){
        directionListener = _directionListener;
    }

    // debug
    public void setAnalysisListener(AnalysisListener _analysisListener) {
        analysisListener = _analysisListener;
    }

    public void setHeadingListener(HeadingListener _headingListener) {
        headingListener = _headingListener;
    }

    public void setDirectionListener(DirectionListener directionListener) {
        this.directionListener = directionListener;
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
    public void newDirectionEvent(DirectionEvent event) {
        if (directionListener != null) {
            this.directionListener.newDirectionEvent(event);
        }
    }

    @Override
    public void newHeading(float heading) {
        if (headingListener != null) headingListener.newHeading(heading);
        sleipnir().newHeading(heading);
    }
}
