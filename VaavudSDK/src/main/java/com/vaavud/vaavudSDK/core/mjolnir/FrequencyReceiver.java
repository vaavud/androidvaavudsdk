package com.vaavud.vaavudSDK.core.mjolnir;

import com.vaavud.vaavudSDK.core.model.FreqAmp;

/**
 * Created by juan on 13/01/16.
 */


public interface FrequencyReceiver {
    void newFrequency(FreqAmp data);
}
