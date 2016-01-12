package com.vaavud.sleipnirSDK.wind;

public class TickProcessor implements TickReceiver{

    private static int TEETH_PR_REV = 15;

    private int startCounter;
    private boolean startLocated;

    private int teethIndex;
    private int teethProcessIndex;
    private int[] times = new int[TEETH_PR_REV];
    private int timeLast;
    private int timeOneRotation;
    private int timeOneRotationLast;

    private float[] velocities = new float[TEETH_PR_REV];
    private float lastVelocity;
    private float[] toothSize = new float[TEETH_PR_REV];

    private RotationReceiver rotationReceiver;
    private int tickDetectionErrorCount;

    private float heading = 0; // should be thread safe since it's 32 bits

    public TickProcessor(RotationReceiver rotationReceiver) {
        this.rotationReceiver = rotationReceiver;

        for (int i = 0; i < toothSize.length; i++) {
            toothSize[i] = i < TEETH_PR_REV-1 ? 23.5f : 31f;
        }

        startLocated = false;
    }


    private void locateStart(int time) {
        if (time > 1.2 * timeLast && time < 1.4 * timeLast) {
            if (startCounter == 2 * TEETH_PR_REV) {
                startLocated = true;
                startCounter = 0;
            }

            if (startCounter % TEETH_PR_REV != 0) {
                startCounter = 0;
                resetDirectionAlgorithm();
            }
        }

        if (startCounter > 2 * TEETH_PR_REV) {
            startCounter = 0;
            resetDirectionAlgorithm();
        }
        
        timeLast = time;
        startCounter++;
    }


    public void updateVelocites(Tick tick) {
        // Moving Avg subtract
        timeOneRotation -= times[teethIndex];

        // Moving avg Update buffer value
        times[teethIndex] = tick.deltaTime;

        // Moving Avg update SUM
        timeOneRotation += times[teethIndex];

        float avgVelocity = 360/(float) timeOneRotation;
        velocities[teethProcessIndex] = velocity(teethProcessIndex)/avgVelocity -1;

        teethIndex++;
        if (teethIndex == TEETH_PR_REV) {
            teethIndex = 0;
            timeOneRotationLast = timeOneRotation;
        }
        teethProcessIndex++;
        if (teethProcessIndex == TEETH_PR_REV) {
            teethProcessIndex = 0;
        }
    }


    @Override
    public void newTick(Tick tick) {
        updateVelocites(tick);
        float velocity = velocity(teethIndex);

        if (!startLocated) {
            locateStart(tick.deltaTime);
        } else {
            if (velocity > 0.8 * lastVelocity && velocity < 1.2 * lastVelocity) {
                if (teethIndex == 0) newRotation(tick);
            } else {
                resetDirectionAlgorithm();
                tickDetectionErrorCount++;
            }
        }

        lastVelocity = velocity;
    }

    private void resetDirectionAlgorithm() {
        for (int i = 0; i < TEETH_PR_REV; i++) {
            times[i] = 0;
        }
        teethProcessIndex = TEETH_PR_REV / 2; //should be 7 for 15;
        teethIndex = 0;
        timeOneRotation = 0;
        startLocated = false;
    }

    private void newRotation(Tick tick) {
        rotationReceiver.newRotation(new Rotation(tick.time, timeOneRotation, relRotaionTime(), heading, velocities));
    }

    private float velocity(int i) {
        return toothSize[i]/(float) times[i];
    }

    private float relRotaionTime() {
        return (float) timeOneRotation / (float) timeOneRotationLast - 1;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public void close() {
        times = null;
        velocities = null;
    }


    public void resetDetectionErrors() {
        tickDetectionErrorCount = 0;
    }

    public int getTickDetectionErrorCount() {
        return tickDetectionErrorCount;
    }

}
