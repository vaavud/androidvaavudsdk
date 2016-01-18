package com.vaavud.vaavudSDK.core;

import android.content.Context;

import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.mjolnir.MjolnirController;
import com.vaavud.vaavudSDK.core.model.SpeedEvent;
import com.vaavud.vaavudSDK.core.orientation.OrientationController;
import com.vaavud.vaavudSDK.core.sleipnir.SleipnirController;
import com.vaavud.vaavudSDK.core.sleipnir.listener.SignalListener;

/**
 * Created by aokholm on 15/01/16.
 */
public class VaavudCoreSDK implements SpeedListener{

    Context context;

    private SpeedListener speedListener;
    private StatusListener statusListener;
    private SignalListener signalListener; // debug

    private SleipnirController _sleipnir;
    private MjolnirController _mjolnir;
    private OrientationController _orientation;

    private boolean sleipnirActive;

    public VaavudCoreSDK(Context context) {
        this.context = context;
    }

    public void startMjolnir() throws VaavudError {
        orientation().start();
        orientation().setMjolnir(true);
        mjolnir().setSpeedListener(this);
        mjolnir().start();
    }

    public void stopMjolnir() {
        mjolnir().stop();
    }

    public void startSleipnir() throws VaavudError {
        orientation().setMjolnir(false);
        orientation().setHeadingListener(sleipnir());

        sleipnir().setSpeedListener(this);
        sleipnir().setSignalListener(signalListener);

        orientation().start();
        sleipnir().start();
    }

    public void stopSleipnir() {
        sleipnir().stop();
        orientation().stop();
    }

    private MjolnirController mjolnir() {
        if (_mjolnir == null) {
            _mjolnir = new MjolnirController(context, orientation());
        }
        return _mjolnir;
    }

    private SleipnirController sleipnir() {
        if (_sleipnir == null) {
            _sleipnir = new SleipnirController(context);
        }
        return _sleipnir;
    }

    private OrientationController orientation() {
        if (_orientation == null) {
            _orientation = new OrientationController(context);
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
    public void setSignalListener(SignalListener signalListener) {
        this.signalListener = signalListener;
    }

    public boolean isSleipnirActive() {
        return sleipnir().isActive();
    }

    @Override
    public void speedChanged(SpeedEvent event) {
        this.speedListener.speedChanged(event);
    }
}
