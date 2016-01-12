package com.vaavud.sleipnirSDK.wind;

/**
 * Created by aokholm on 12/01/16.
 */
public class Throttle {

    long timeNext;
    int delta;

    public Throttle(int delta) {
        this.delta = delta;
        timeNext = 0;
    }

    boolean shouldSend(long time) {
        if (timeNext == 0) {
            timeNext = time + delta;
            return true;
        }

        if (time > timeNext) {
            long tempTimeNext = timeNext + delta;
            if (time > tempTimeNext) {
                timeNext = time + delta;
            } else {
                timeNext = tempTimeNext;
            }
            return true;
        }

        return false;
    }

}
