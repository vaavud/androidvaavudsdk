package com.vaavud.vaavudSDK.core.sleipnir;

/**
 * Created by aokholm on 19/01/16.
 */
public class RelativeTime {
    long firstTime;

    public RelativeTime() {
        firstTime = 0;
    }

    public float relTime(long time) {
        if (firstTime == 0) {
            firstTime = time;
        }

        float relTime = ((float) (time-firstTime))/1000;

        return relTime;
    }
}
