package com.vaavud.vaavudSDK;


import android.content.Context;
import android.util.Log;

import com.vaavud.vaavudSDK.core.VaavudCoreSDK;
import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.DirectionListener;
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
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;
import com.vaavud.vaavudSDK.model.MeasurementSession;
import com.vaavud.vaavudSDK.model.WindMeter;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;

import java.util.Date;
import java.util.Map;



/**
 * Created by juan on 18/01/16.
 */
public class VaavudSDK implements SpeedListener, DirectionListener, LocationEventListener, OrientationListener, StatusListener, PlugListener {

    private static final String TAG = "VaavudSDK";
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
        config = new Config(configuration);
    }

    public boolean isSleipnirAvailable(){
        return sleipnirAvailable;
    }

    public int startSession() throws VaavudError {

        windSpeed = 0.0f;

        session = new MeasurementSession();
        session.startSession();
        location().setEventListener(this);
        location().start();
        if (config.getWindMeter().equals(WindMeter.MJOLNIR)) sdk.startMjolnir();
        if (config.getWindMeter().equals(WindMeter.SLEIPNIR) & sleipnirAvailable ) sdk.startSleipnir();

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
//
//
//    public void setOrientationListener(OrientationListener orientationListener) {
//        sdk.setOrientationListener(this);
//        vaavudOrientation = orientationListener;
//    }
//    public void setHeadingListener(HeadingListener headingListener) {
//        sdk.setHeadingListener(headingListener);
//    }
//
//    public void setDirectionListener(DirectionListener directionListener){
//        sdk.setDirectionListener(directionListener);
//    }
//
//    public void setAnalysisListener(AnalysisListener analysisListener){
//        sdk.setAnalysisListener(analysisListener);
//    }
    // FIXME: 21/01/16 END fix


    public VaavudCoreSDK getSdk() {
        return sdk;
    }

    @Override
    public void speedChanged(SpeedEvent event) {
        windSpeed = 0.8f * windSpeed + 0.2f*event.getSpeed();
        session.addSpeedEvent(event);
        vaavudSpeed.speedChanged(new SpeedEvent(event.getTime(),windSpeed));
    }

    @Override
    public void newDirectionEvent(DirectionEvent event) {
        session.addDirectionEvent(event);
        vaavudDirection.newDirectionEvent(event);
    }

    @Override
    public void statusChanged(MeasureStatus status) {

    }

    @Override
    public void newLocation(LocationEvent event) {
        Log.d(TAG, "New Location: " + event.getLocation());
        session.addLocationEvent(event);
    }

    @Override
    public void newVelocity(VelocityEvent event) {
        Log.d(TAG, "New Velocity: " + event.getVelocity());
        session.addVelocityEvent(event);
        estimateTrueWind(event);
    }

    @Override
    public void newBearing(BearingEvent event) {
        session.addBearingEvent(event);
    }

    private void estimateTrueWind(VelocityEvent velocity) {
        DirectionEvent direction = session.getLastDirectionEvent();
        SpeedEvent speed = session.getLastSpeedEvent();

        float rad = (float) ((Math.PI * direction.getDirection())/180);
        float trueSpeed = (float)Math.sqrt(Math.pow(speed.getSpeed(),2.0) + Math.pow(velocity.getVelocity(),2) - 2*speed.getSpeed()*velocity.getVelocity()*Math.cos(rad));
        session.addTrueSpeedEvent(new TrueSpeedEvent(new Date().getTime(),trueSpeed));

        float trueDirection = 0;
        if (0 < rad && Math.PI > rad) {
            trueDirection = (float) Math.acos((speed.getSpeed() * Math.cos(rad) - velocity.getVelocity()) / trueSpeed);
            session.addTrueDirectionEvent(new TrueDirectionEvent(new Date().getTime(), trueDirection));
        }
        else{
            trueDirection = (-1)*(float)Math.acos((speed.getSpeed()*Math.cos(rad) - velocity.getVelocity())/trueSpeed);
            session.addTrueDirectionEvent(new TrueDirectionEvent(new Date().getTime(),trueDirection));
        }

//        Log.d(TAG,"True Speed: " + trueSpeed + "True Direction: " + trueDirection);

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
