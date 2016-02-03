package com.vaavud.vaavudSDK.core.listener;

import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;

/**
 * Created by juan on 20/01/16.
 */
public interface DirectionListener {
		void newDirectionEvent(DirectionEvent event);
		void trueDirectionEvent(TrueDirectionEvent event);
}
