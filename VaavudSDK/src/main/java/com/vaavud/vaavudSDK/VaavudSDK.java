package com.vaavud.vaavudSDK;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


import com.vaavud.vaavudSDK.core.VaavudCoreSDK;
import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.DirectionListener;
import com.vaavud.vaavudSDK.core.listener.HeadingListener;
import com.vaavud.vaavudSDK.core.listener.LocationEventListener;
import com.vaavud.vaavudSDK.core.listener.OrientationListener;
import com.vaavud.vaavudSDK.core.listener.PlugListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.location.LocationService;
import com.vaavud.vaavudSDK.core.model.MeasureStatus;
import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.sleipnir.audio.HeadsetIntentReceiver;
import com.vaavud.vaavudSDK.core.sleipnir.listener.AnalysisListener;
import com.vaavud.vaavudSDK.model.MeasurementSession;
import com.vaavud.vaavudSDK.model.WindMeter;
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;

import java.util.Date;
import java.util.Map;


/**
 * Created by juan on 18/01/16.
 */
public class VaavudSDK implements SpeedListener, DirectionListener, LocationEventListener, OrientationListener, StatusListener, PlugListener {

    private static final String TAG = "VaavudSDK";
    private final HeadsetIntentReceiver receiver;
    private Context context;
    private VaavudCoreSDK sdk;
    private MeasurementSession session;
    private Config config;
    private LocationService _location;

    private SpeedListener vaavudSpeed;
    private DirectionListener vaavudDirection;
    private LocationEventListener vaavudLocation;
    private OrientationListener vaavudOrientation;


    private boolean sleipnirAvailable = false;
    private Float windSpeed;
    private Float windDirection;

    public VaavudSDK(Context _context, Map<String, Object> configuration) {
        context = _context;
        if (sdk == null) {
            sdk = new VaavudCoreSDK(context);
        }
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        receiver = new HeadsetIntentReceiver(this);
        context.registerReceiver(receiver, receiverFilter);
        config = new Config(configuration);
    }

    public boolean isSleipnirAvailable() {
        return sleipnirAvailable;
    }

    public int startSession() throws VaavudError {

        windSpeed = 0.0f;

        session = new MeasurementSession();
        session.startSession();
        location().setEventListener(this);
        location().start();
        if (config.getWindMeter().equals(WindMeter.MJOLNIR)) sdk.startMjolnir();
        if (config.getWindMeter().equals(WindMeter.SLEIPNIR) & sleipnirAvailable)
            sdk.startSleipnir();

        return 0;

    }

    public void stopSession() throws VaavudError {

        session.stopSession();
        location().stop();
        if (config.getWindMeter().equals(WindMeter.SLEIPNIR)) sdk.stopSleipnir();
        else sdk.stopMjolnir();

    }

    private LocationService location() {
        if (_location == null) {
            _location = new LocationService(context, config.getLocationFrequency());
        }
        return _location;
    }

    public boolean isRunning() {
        return sdk.isSleipnirActive();
    }


    // // FIXME: 21/01/16 concider removing and ask users to access to the SDKCore directly
//
    public void setSpeedListener(SpeedListener speedListener) {
        sdk.setSpeedListener(this);
        vaavudSpeed = speedListener;
    }

    public void setDirectionListener(DirectionListener directionListener) {
        sdk.setDirectionListener(this);
        vaavudDirection = directionListener;
    }

    public void setLocationListener(LocationEventListener locationListener) {
        vaavudLocation = locationListener;
//        location().setEventListener(vaavudLocation);
    }
//
//
    public void setOrientationListener(OrientationListener orientationListener) {
        sdk.setOrientationListener(this);
        vaavudOrientation = orientationListener;
    }
    public void setHeadingListener(HeadingListener headingListener) {
        sdk.setHeadingListener(headingListener);
    }

    public void setAnalysisListener(AnalysisListener analysisListener){
        sdk.setAnalysisListener(analysisListener);
    }
    // FIXME: 21/01/16 END fix


    public VaavudCoreSDK getSdk() {
        return sdk;
    }

    @Override
    public void speedChanged(SpeedEvent event) {
        windSpeed = 0.8f * windSpeed + 0.2f * event.getSpeed();
        session.addSpeedEvent(event);
        vaavudSpeed.speedChanged(new SpeedEvent(event.getTime(), windSpeed));
    }

    @Override
    public void trueSpeedChanged(TrueSpeedEvent event) {


    }

    @Override
    public void newDirectionEvent(DirectionEvent event) {
        session.addDirectionEvent(event);
        vaavudDirection.newDirectionEvent(event);
    }

    @Override
    public void trueDirectionEvent(TrueDirectionEvent event) {

    }

    @Override
    public void statusChanged(MeasureStatus status) {

    }

    @Override
    public void newLocation(LocationEvent event) {
        Log.d(TAG, "New Location: " + event.getLocation());
        session.addLocationEvent(event);
        if (vaavudLocation!=null)
            vaavudLocation.newLocation(event);
    }

    @Override
    public void newVelocity(VelocityEvent event) {
        Log.d(TAG, "New Velocity: " + event.getVelocity());

        session.addVelocityEvent(event);
        estimateTrueWind(event);
        if (vaavudLocation!=null)
            vaavudLocation.newVelocity(event);
    }

    @Override
    public void newBearing(BearingEvent event) {
        if (event.getBearing() != 0.0f) {
            session.addBearingEvent(event);
            if (vaavudLocation!=null)
                vaavudLocation.newBearing(event);
        }
    }

    private void estimateTrueWind(VelocityEvent velocity) {
        DirectionEvent direction = session.getLastDirectionEvent();
        SpeedEvent speed = session.getLastSpeedEvent();
        BearingEvent bearing = session.getLastBearingEvent();

        if (direction != null && speed != null && bearing != null) {

            float alpha = direction.getDirection() - bearing.getBearing();
            float rad = (float) ((Math.PI * alpha) / 180);
            float trueSpeed = (float) Math.sqrt(Math.pow(speed.getSpeed(), 2.0) + Math.pow(velocity.getVelocity(), 2) - 2 * speed.getSpeed() * velocity.getVelocity() * Math.cos(rad));
            if (trueSpeed >= 0) {
                TrueSpeedEvent speedEvent = new TrueSpeedEvent(new Date().getTime(), trueSpeed);
                session.addTrueSpeedEvent(speedEvent);
                vaavudSpeed.trueSpeedChanged(speedEvent);
            }

            float trueDirection = -1;
            TrueDirectionEvent directionEvent = null;
            if (0 < rad && Math.PI > rad) {
                trueDirection = (float) Math.acos((speed.getSpeed() * Math.cos(rad) - velocity.getVelocity()) / trueSpeed);
            } else {
                trueDirection = (-1) * (float) Math.acos((speed.getSpeed() * Math.cos(rad) - velocity.getVelocity()) / trueSpeed);

            }
            trueDirection = (float) ((trueDirection*180)/Math.PI);
            if (trueDirection != -1) {
                directionEvent = new TrueDirectionEvent(new Date().getTime(), trueDirection);
                session.addTrueDirectionEvent(directionEvent);
                vaavudDirection.trueDirectionEvent(directionEvent);
                Log.d(TAG,"True Speed: " + trueSpeed + "True Direction: " + trueDirection);
            }

        }else{
            vaavudSpeed.trueSpeedChanged(new TrueSpeedEvent(0,0));
            vaavudDirection.trueDirectionEvent(new TrueDirectionEvent(0,0));
        }

    }

    @Override
    public void finalize(){
        context.unregisterReceiver(receiver);
    }

    @Override
    public void permisionError(String permission) {
        Log.d(TAG, "Permission Error: " + permission);
    }

    @Override
    public void newOrientation(float x, float y, float z) {

    }

    @Override
    public void onHeadsetStatusChanged(boolean plugged) {
        sleipnirAvailable = plugged;
    }

}
