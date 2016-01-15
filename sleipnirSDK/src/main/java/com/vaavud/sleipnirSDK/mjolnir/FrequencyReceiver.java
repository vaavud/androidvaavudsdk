package com.vaavud.sleipnirSDK.mjolnir;

import com.vaavud.sleipnirSDK.model.FreqAmp;

/**
 * Created by juan on 13/01/16.
 */


public interface FrequencyReceiver {
		void newFrequency(FreqAmp data);
}
