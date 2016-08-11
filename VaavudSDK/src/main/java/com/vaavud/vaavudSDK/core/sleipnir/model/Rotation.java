package com.vaavud.vaavudSDK.core.sleipnir.model;

/**
 * Created by aokholm on 20/01/16.
 */
public class Rotation {
    public final long time;
    public final int timeOneRotation;
    public final float relRotationTime;
    public final Float heading;
    public final float[] relVelocities;

    public Rotation(long time, int timeOneRotation, float relRotationTime, Float heading, float[] relVelocities) {
        this.time = time;
        this.timeOneRotation = timeOneRotation;
        this.relRotationTime = relRotationTime;
        this.heading = heading;
        this.relVelocities = relVelocities;
    }
}
