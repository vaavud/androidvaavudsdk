package com.vaavud.vaavudSDK.core.model.event;

/**
 * Created by aokholm on 12/01/16.
 */
public class SpeedEvent {
    long time;
    float speed;

    public SpeedEvent() {
    }

    public SpeedEvent(long _time, float _speed) {
        time = _time;
        speed = _speed;
    }

    public long getTime() {
        return time;
    }

    public float getSpeed() {
        return speed;
    }
}
