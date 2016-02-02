package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 29/01/16.
 */
public class TemperatureEvent {
    long time;
    float temperature;

    public TemperatureEvent() {
    }

    public TemperatureEvent(long _time, float _temperature) {
        time = _time;
        temperature = _temperature;
    }

    public long getTime() {
        return time;
    }

    public float getTemperature() {
        return temperature;
    }
}

