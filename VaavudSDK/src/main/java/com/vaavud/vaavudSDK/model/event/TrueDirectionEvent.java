package com.vaavud.vaavudSDK.model.event;

/**
 * Created by juan on 29/01/16.
 */
public class TrueDirectionEvent {
    long time;
    float trueDirection;

    public TrueDirectionEvent() {
    }

    public TrueDirectionEvent(long _time, float _trueDirection) {
        time = _time;
        trueDirection = _trueDirection;
    }

    public long getTime() {
        return time;
    }

    public float getTrueDirection() {
        return trueDirection;
    }
}

