package com.vaavud.vaavudSDK.model;

/**
 * Created by juan on 14/01/16.
 */
public class FreqAmp {

    public final float frequency;
    public final float amplitude;
    public final long time;

    public FreqAmp(long _time, float _frequency, float _amplitude) {
        time = _time;
        frequency = _frequency;
        amplitude = _amplitude;
    }

}