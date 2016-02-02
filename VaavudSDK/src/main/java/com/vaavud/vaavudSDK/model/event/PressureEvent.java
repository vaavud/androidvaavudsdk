package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 29/01/16.
 */

public class PressureEvent {
    long time;
    float pressure;

    public PressureEvent() {
    }

    public PressureEvent(long _time, float _pressure) {
        time = _time;
        pressure = _pressure;
    }

    public long getTime() {
        return time;
    }

    public float getPressure() {
        return pressure;
    }
}

