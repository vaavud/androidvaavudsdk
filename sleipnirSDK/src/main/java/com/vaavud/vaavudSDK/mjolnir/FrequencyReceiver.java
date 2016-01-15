package com.vaavud.vaavudSDK.mjolnir;

import com.vaavud.vaavudSDK.model.FreqAmp;

/**
 * Created by juan on 13/01/16.
 */


public interface FrequencyReceiver {
    void newFrequency(FreqAmp data);
}
