package com.vaavud.vaavudSDK;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.vaavud.vaavudSDK.core.VaavudCoreSDK;
import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.DirectionListener;
import com.vaavud.vaavudSDK.core.listener.HeadingListener;
import com.vaavud.vaavudSDK.core.listener.LocationEventListener;
import com.vaavud.vaavudSDK.core.listener.OrientationListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.location.LocationService;
import com.vaavud.vaavudSDK.core.model.MeasureStatus;
import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.model.event.VelocityEvent;
import com.vaavud.vaavudSDK.core.sleipnir.listener.AnalysisListener;
import com.vaavud.vaavudSDK.model.MeasurementSession;
import com.vaavud.vaavudSDK.model.WindMeter;

import java.util.Iterator;
import java.util.Map;



/**
 * Created by juan on 18/01/16.
 */
public class VaavudSDK implements SpeedListener, DirectionListener, LocationEventListener, OrientationListener, StatusListener {

    private static final String TAG = "VaavudSDK";
    private Context context;
    private VaavudCoreSDK sdk;
    private MeasurementSession session;
    private Config config;
    private LocationService _location;

//    private SpeedListener vaavudSpeed;
//    private DirectionListener vaavudDirection;
//    private LocationEventListener vaavudLocation;
//    private OrientationListener vaavudOrientation;


    private Float windSpeedAvg;
    private Float windSpeedMax;
    private Float windDirection;



    private final Handler handler;

    private Runnable sendData = new Runnable() {
        @Override
        public void run(){
            Log.d(TAG, String.valueOf(session.getLastSpeedEvent().getTime()));
//            if (vaavudSpeed!=null) vaavudSpeed.speedChanged(session.getLastSpeedEvent());

//            if (vaavudDirection!=null) vaavudDirection.newDirectionEvent(session.getLastDirectionEvent());
//            if (vaavudLocation!=null) vaavudLocation.newLocation(session.getLastLocationEvent());
            handler.postDelayed(this,config.getUpdateFrequency());
        }
    };


    public VaavudSDK(Context _context, Map<String, Object> configuration) {
        context = _context;
        if (sdk == null) {
            sdk = new VaavudCoreSDK(context);
            sdk.setSpeedListener(this);
        }
        config = new Config(configuration);
        handler = new Handler();

    }

    public void startSession() throws VaavudError {
        windSpeedAvg = 0.0f;
        windSpeedMax = 0.0f;

        session = new MeasurementSession();
        session.startSession();
        location().setEventListener(this);
        location().start();

        if (config.getWindMeter().equals(WindMeter.SLEIPNIR)) sdk.startSleipnir();
        else sdk.startMjolnir();

        handler.postDelayed(sendData,config.getUpdateFrequency());

    }

    public void stopSession() throws VaavudError {

        Log.d(TAG,"SpeedAverage: "+windSpeedAvg);
        Log.d(TAG,"SpeedMax: "+windSpeedMax);
        handler.removeCallbacks(sendData);
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
//    public void setSpeedListener(SpeedListener speedListener) {
//        sdk.setSpeedListener(this);
//        vaavudSpeed = speedListener;
//    }
//
//    public void setDirectionListener(DirectionListener directionListener) {
//        sdk.setDirectionListener(this);
//        vaavudDirection = directionListener;
//    }
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
        if (event.getSpeed() > windSpeedMax) windSpeedMax = event.getSpeed();
        windSpeedAvg = (event.getSpeed() + session.getNumSpeedEvents() * windSpeedAvg) / (session.getNumSpeedEvents() + 1);
        session.addSpeedEvent(event);
//        vaavudSpeed.speedChanged(event);
    }

    @Override
    public void newDirectionEvent(DirectionEvent event) {
        session.addDirectionEvent(event);
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
    }

    @Override
    public void permisionError(String permission) {
        Log.d(TAG, "Permission Error: " + permission);
    }

    @Override
    public void newOrientation(float x, float y, float z) {

    }


    class Config {
        private WindMeter windMeter;
        private int updateFrequency;
        private long locationFrequency;

        public Config(Map<String, Object> configuration) {
            if (configuration != null) {
                configure(configuration);
            } else {
                windMeter = WindMeter.SLEIPNIR;
                updateFrequency = 200;
                locationFrequency = 1000;
            }

        }

        public WindMeter getWindMeter() {
            return windMeter;
        }

        public void setWindMeter(WindMeter _windMeter) {
            windMeter = _windMeter;
        }

        public int getUpdateFrequency() {
            return updateFrequency;
        }

        public void setUpdateFrequency(int _updateFrequency) {
            updateFrequency = _updateFrequency;
        }

        public long getLocationFrequency() {
            return locationFrequency;
        }

        public void setLocationFrequency(int _locationFrequency) {
            locationFrequency = _locationFrequency;
        }

        private void configure(Map<String, Object> configuration) {
            Iterator<Map.Entry<String, Object>> it = configuration.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                switch (entry.getKey()) {
                    case "windMeter":
                        config.setWindMeter((WindMeter) entry.getValue());
                        break;
                    case "updateFrequency":
                        config.setUpdateFrequency((int) entry.getValue());
                        break;
                    case "locationFrequency":
                        config.setUpdateFrequency((int) entry.getValue());
                        break;
                    default:
                        break;
                }

            }
        }
    }
}
