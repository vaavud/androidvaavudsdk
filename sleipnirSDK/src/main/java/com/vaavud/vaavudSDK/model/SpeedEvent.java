package com.vaavud.vaavudSDK.model;

/**
 * Created by aokholm on 12/01/16.
 */
public class SpeedEvent {
    long time;
    float speed;

    public SpeedEvent() {
    }

    public SpeedEvent(long time, float speed) {
        this.time = time;
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public float getSpeed() {
        return speed;
    }
}
