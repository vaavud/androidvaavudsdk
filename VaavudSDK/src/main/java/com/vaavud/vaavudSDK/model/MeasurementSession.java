package com.vaavud.vaavudSDK.model;

/**
 * Created by juan on 18/01/16.
 */

import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.HeadingEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.model.event.AltitudeEvent;
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.PressureEvent;
import com.vaavud.vaavudSDK.model.event.TemperatureEvent;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasurementSession implements Serializable {

    private static final String TAG = "MeaSession";
    private String geoLocationNameLocalized;
    private long startTime;
    private long endTime;
    private float windMean;
    private float windMax;
    private float windDirection = -1;
    private float trueWindMean;
    private float trueWindMax;
    private float trueWindDirection;

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
    private int lastTrueDirectionDispached = 0;


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
        windMean = 0.8f * windMean + 0.2f * event.getSpeed();
        if (windMax < windMean) windMax = windMean;
        speed.add(event);
    }

    public void addLocationEvent(LocationEvent event) {
        location.add(event);
    }

    public void addDirectionEvent(DirectionEvent event) {
        windDirection = event.getDirection();
        direction.add(event);
    }

    public void addVelocityEvent(VelocityEvent event) {
        velocity.add(event);
    }

    public void addTrueSpeedEvent(TrueSpeedEvent event) {
        trueWindMean = 0.8f * trueWindMean + 0.2f * event.getTrueSpeed();
        if (trueWindMax < trueWindMean) trueWindMax = trueWindMean;
        trueSpeed.add(event);
    }

    public void addTrueDirectionEvent(TrueDirectionEvent event) {
        trueWindDirection = event.getTrueDirection();
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

    public SpeedEvent getLastSpeedEvent() {
        if (speed.size() > 0) {
            return speed.get(speed.size() - 1);
        }
        return null;
    }

    public TrueSpeedEvent getLastTrueSpeedEvent() {
        if (trueSpeed.size() > 0) {
            return trueSpeed.get(trueSpeed.size() - 1);
        }
        return null;
    }

    public TrueDirectionEvent getLastTrueDirectionEvent() {
        if (trueDirection.size() > 0) {
            return trueDirection.get(trueDirection.size() - 1);
        }
        return null;
    }

    public LocationEvent getLastLocationEvent() {
        if (location.size() > 0) {
            return location.get(location.size() - 1);
        }
        return null;
    }

    public DirectionEvent getLastDirectionEvent() {
        if (direction.size() > 0) {
            return direction.get(direction.size() - 1);
        }
        return null;
    }

    public BearingEvent getLastBearingEvent() {
        if (bearing.size() > 0) {
            return bearing.get(bearing.size() - 1);
        }
        return null;
    }

    public MeasurementSession stopSession() {
        endTime = new Date().getTime();
        return this;
    }

    public float getWindMean() {
        return windMean;
    }

    public float getWindMax() {
        return windMax;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public float getTrueWindMean() {
        return trueWindMean;
    }

    public float getTrueWindDirection() {
        return trueWindDirection;
    }

    public List<LocationEvent> getLocation() {
        return location;
    }

    public List<SpeedEvent> getSpeed() {
        return speed;
    }

    public List<DirectionEvent> getDirection() {
        return direction;
    }

    public List<VelocityEvent> getVelocity() {
        return velocity;
    }

    public List<HeadingEvent> getHeading() {
        return heading;
    }

    public List<BearingEvent> getBearing() {
        return bearing;
    }

    public List<TrueSpeedEvent> getTrueSpeed() {
        return trueSpeed;
    }

    public List<TrueDirectionEvent> getTrueDirection() {
        return trueDirection;
    }

    public List<TemperatureEvent> getTemperature() {
        return temperature;
    }

    public List<PressureEvent> getPressure() {
        return pressure;
    }

    public List<AltitudeEvent> getAltitude() {
        return altitude;
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
