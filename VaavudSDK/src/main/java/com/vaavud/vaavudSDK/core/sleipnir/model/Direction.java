package com.vaavud.vaavudSDK.core.sleipnir.model;

/**
 * Created by aokholm on 20/01/16.
 */

public class Direction {
    public final long time;
    public final float direction;

    public Direction(long time, float direction) {
        this.time = time;
        this.direction = direction;
    }
}