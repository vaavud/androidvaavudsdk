package com.vaavud.vaavudSDK.core.sleipnir.model;

/**
 * Created by aokholm on 20/01/16.
 */
public class Tick {
    public final long time;
    public final int deltaTime;

    public Tick(long time, int deltaTime) {
        this.time = time;
        this.deltaTime = deltaTime;
    }
}

