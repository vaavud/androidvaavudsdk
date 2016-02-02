package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 29/01/16.
 */
public class AltitudeEvent {

    long time;
    float altitude;

    public AltitudeEvent() {
    }

    public AltitudeEvent(long _time, float _altitude) {
        time = _time;
        altitude = _altitude;
    }

    public long getTime() {
        return time;
    }

    public float getAltitude() {
        return altitude;
    }
}