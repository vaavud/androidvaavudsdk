package com.vaavud.vaavudSDK.model;

/**
 * Created by juan on 18/01/16.
 */

import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.HeadingEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;
import com.vaavud.vaavudSDK.model.event.AltitudeEvent;
import com.vaavud.vaavudSDK.model.event.PressureEvent;
import com.vaavud.vaavudSDK.model.event.TemperatureEvent;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasurementSession implements Serializable {

    private static final String TAG = "MeaSession";
    private String geoLocationNameLocalized;
    private long startTime;
    private long endTime;
    private List<LocationEvent> location;
    private List<SpeedEvent> speed;
    private List<DirectionEvent> direction;
    private List<VelocityEvent> velocity;
    private List<HeadingEvent> heading;
    private List<BearingEvent> bearing;
    private List<TrueSpeedEvent> trueSpeed;
    private List<TrueDirectionEvent> trueDirection;
    private List<TemperatureEvent> temperature;
    private List<PressureEvent> pressure;
    private List<AltitudeEvent> altitude;

    private int lastSpeedDispached = 0;
    private int lastTrueSpeedDispached = 0;
    private int lastTrueDirectionDispached =0;


    private WindMeter windMeter = WindMeter.MJOLNIR;

    private Float windChill;
    private Float gustiness;



    public MeasurementSession() {
        super();
        speed = new ArrayList<>();
        location = new ArrayList<>();
        direction = new ArrayList<>();
        velocity = new ArrayList<>();
        bearing = new ArrayList<>();
        trueSpeed = new ArrayList<>();
        trueDirection = new ArrayList<>();
        temperature = new ArrayList<>();
        pressure = new ArrayList<>();
        altitude = new ArrayList<>();

    }

    public void startSession() {
        speed.clear();
        location.clear();
        direction.clear();
        velocity.clear();
        trueDirection.clear();
        trueSpeed.clear();
        temperature.clear();
        pressure.clear();
        altitude.clear();
        startTime = new Date().getTime();
    }

    public void addSpeedEvent(SpeedEvent event) {
        speed.add(event);
    }
    public void addLocationEvent(LocationEvent event) {
        location.add(event);
    }
    public void addDirectionEvent(DirectionEvent event) {
        direction.add(event);
    }
    public void addVelocityEvent(VelocityEvent event) {
        velocity.add(event);
    }
    public void addTrueSpeedEvent(TrueSpeedEvent event) {
        trueSpeed.add(event);
    }
    public void addTrueDirectionEvent(TrueDirectionEvent event) {
        trueDirection.add(event);
    }
    public void addTemperatureEvent(TemperatureEvent event) {
        temperature.add(event);
    }
    public void addPressureEvent(PressureEvent event) {
        pressure.add(event);
    }
    public void addAltitudeEvent(AltitudeEvent event) {
        altitude.add(event);
    }
    public void addBearingEvent(BearingEvent event) {
        bearing.add(event);
    }

    public SpeedEvent getLastSpeedEvent(){
        int newIndex = speed.size()-1;
//        Log.d(TAG,"last: "+lastSpeedDispached + " new: "+newIndex);

        if (lastSpeedDispached <= newIndex){
            lastSpeedDispached = newIndex;
            return speed.get(lastSpeedDispached);
        }
        return new SpeedEvent(0,0);
    }

    public TrueSpeedEvent getLastTrueSpeedEvent(){
        int newIndex = trueSpeed.size()-1;
//        Log.d(TAG,"last: "+lastSpeedDispached + " new: "+newIndex);

        if (lastTrueSpeedDispached <= newIndex){
            lastTrueSpeedDispached = newIndex;
            return trueSpeed.get(lastTrueSpeedDispached);
        }
        return new TrueSpeedEvent(0,0);
    }

    public TrueDirectionEvent getLastTrueDirectionEvent(){
        int newIndex = trueDirection.size()-1;
//        Log.d(TAG,"last: "+lastSpeedDispached + " new: "+newIndex);

        if (lastTrueDirectionDispached <= newIndex){
            lastTrueDirectionDispached = newIndex;
            return trueDirection.get(lastTrueDirectionDispached);
        }
        return new TrueDirectionEvent(0,-1);
    }

    public LocationEvent getLastLocationEvent(){

        if (location.size()>0) {
            return location.get(location.size() - 1);
        }else{
            return null;
        }
    }
    public DirectionEvent getLastDirectionEvent(){
        if (direction.size()>0) {
            return direction.get(direction.size() - 1);
        }else{
            return null;
        }
    }

    public BearingEvent getLastBearingEvent(){
        if (bearing.size()>0) {
            return bearing.get(bearing.size() - 1);
        }else{
            return null;
        }
    }

    public MeasurementSession stopSession() {
        endTime = new Date().getTime();
        return this;
    }

    public int getNumSpeedEvents() {
        return speed.size();
    }

    public void setWindChill(Float windChill) {
        this.windChill = windChill;
    }

    public Float getWindChill() {
        return windChill;
    }

    public void setGustiness(Float gustiness) {
        this.gustiness = gustiness;
    }

    public Float getGustiness() {
        return gustiness;
    }

    public String getGeoLocationNameLocalized() {
        return geoLocationNameLocalized;
    }

    public void setGeoLocationNameLocalized(String geoLocationNameLocalized) {
        this.geoLocationNameLocalized = geoLocationNameLocalized;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public WindMeter getWindMeter() {
        return windMeter;
    }

    public void setWindMeter(WindMeter windMeter) {
        this.windMeter = windMeter;
    }



}
