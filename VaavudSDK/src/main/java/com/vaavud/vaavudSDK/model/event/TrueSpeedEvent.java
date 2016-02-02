package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 29/01/16.
 */
public class TrueSpeedEvent {
    long time;
    float trueSpeed;

    public TrueSpeedEvent() {
    }

    public TrueSpeedEvent(long _time, float _trueSpeed) {
        time = _time;
        trueSpeed = _trueSpeed;
    }

    public long getTime() {
        return time;
    }

    public float getTrueSpeed() {
        return trueSpeed;
    }
}

