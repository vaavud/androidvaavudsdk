package com.vaavud.vaavudSDK.core.listener;

import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;
import com.vaavud.vaavudSDK.model.event.BearingEvent;

/**
 * Created by juan on 19/01/16.
 */
public interface LocationEventListener {
		void newLocation(LocationEvent event);
		void newVelocity(VelocityEvent event);
		void newBearing(BearingEvent event);
		void permisionError(String permission);
}
