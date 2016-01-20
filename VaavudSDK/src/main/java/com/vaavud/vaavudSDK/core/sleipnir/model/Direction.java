package com.vaavud.vaavudSDK.core.sleipnir.model;

/**
 * Created by aokholm on 20/01/16.
 */

public class Direction {
    public final long time;
    public final float globalDirection;
    public final float heading;

    public Direction(long time, float globalDirection, float heading) {
        this.time = time;
        this.globalDirection = globalDirection;
        this.heading = heading;
    }
}