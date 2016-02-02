package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 02/02/16.
 */
public class BearingEvent {
    long time;
    float bearing;

    public BearingEvent() {
    }

    public BearingEvent(long _time, float _bearing) {
        time = _time;
        bearing = _bearing;
    }

    public long getTime() {
        return time;
    }

    public float getBearing() {
        return bearing;
    }
}