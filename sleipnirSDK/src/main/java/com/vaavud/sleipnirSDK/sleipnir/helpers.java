package com.vaavud.sleipnirSDK.sleipnir;

/**
 * Created by aokholm on 14/01/16.
 */

class Tick {
    public final long time;
    public final int deltaTime;

    public Tick(long time, int deltaTime) {
        this.time = time;
        this.deltaTime = deltaTime;
    }
}

class Rotation {
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


class Direction {
    public final long time;
    public final float globalDirection;
    public final float heading;

    public Direction(long time, float globalDirection, float heading) {
        this.time = time;
        this.globalDirection = globalDirection;
        this.heading = heading;
    }
}

interface TickReceiver {
    void newTick(Tick tick);
}

interface RotationReceiver {
    void newRotation(Rotation rotation);
}

interface DirectionReceiver {
    void newDirection(Direction direction);
}
