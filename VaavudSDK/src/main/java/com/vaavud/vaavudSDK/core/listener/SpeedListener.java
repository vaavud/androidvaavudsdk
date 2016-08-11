package com.vaavud.vaavudSDK.core.listener;

import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;

public interface SpeedListener {

    void speedChanged(SpeedEvent event);
    void trueSpeedChanged(TrueSpeedEvent event);
}
