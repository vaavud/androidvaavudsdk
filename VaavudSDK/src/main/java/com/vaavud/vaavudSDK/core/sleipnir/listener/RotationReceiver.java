package com.vaavud.vaavudSDK.core.sleipnir.listener;

import com.vaavud.vaavudSDK.core.sleipnir.model.Rotation;

/**
 * Created by aokholm on 20/01/16.
 */

public interface RotationReceiver {
    void newRotation(Rotation rotation);
}