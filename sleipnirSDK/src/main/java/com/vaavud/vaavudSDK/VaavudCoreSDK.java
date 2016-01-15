package com.vaavud.vaavudSDK;

import android.content.Context;

import com.vaavud.vaavudSDK.listener.SpeedListener;
import com.vaavud.vaavudSDK.listener.StatusListener;
import com.vaavud.vaavudSDK.mjolnir.MjolnirController;
import com.vaavud.vaavudSDK.sleipnir.SleipnirController;

/**
 * Created by aokholm on 15/01/16.
 */
public class VaavudCoreSDK {

    private Context context;

    private SpeedListener speedListener;
    private StatusListener statusListener;

    private SleipnirController sleipnirCont;
    private MjolnirController mjolnirCont;

    public VaavudCoreSDK(Context context) {
        this.context = context;
    }

    public void startMjolnir() throws VaavudError {
        if (mjolnirCont == null) {
            mjolnirCont = new MjolnirController(context);
        }
        mjolnirCont.startMeasuring();
    }

    public void stopMjolnir() {
        mjolnirCont.stopMeasuring();
    }

    public void startSleipnir() throws VaavudError{
        if (sleipnirCont == null) {
            sleipnirCont = new SleipnirController(context);
        }
        sleipnirCont.startMeasuring();
    }

    public void stopSleipnir() {
        sleipnirCont.stopMeasuring();
    }
}
