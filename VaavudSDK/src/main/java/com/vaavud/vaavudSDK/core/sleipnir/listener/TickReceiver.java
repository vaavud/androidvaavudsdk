package com.vaavud.vaavudSDK.core.sleipnir.listener;

import com.vaavud.vaavudSDK.core.sleipnir.model.Tick;

/**
 * Created by aokholm on 20/01/16.
 */
public interface TickReceiver {
    void newTick(Tick tick);
}
