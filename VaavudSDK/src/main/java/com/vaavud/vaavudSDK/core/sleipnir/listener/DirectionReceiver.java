package com.vaavud.vaavudSDK.core.sleipnir.listener;

import com.vaavud.vaavudSDK.core.sleipnir.model.Direction;

/**
 * Created by aokholm on 20/01/16.
 */

public interface DirectionReceiver {
    void newDirection(Direction direction);
}
