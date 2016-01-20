package com.vaavud.vaavudSDK.core.model.event;

import com.vaavud.vaavudSDK.core.model.LatLng;

/**
 * Created by juan on 19/01/16.
 */
public class LocationEvent {
		long time;
		LatLng location;

		public LocationEvent() {
		}

		public LocationEvent(long _time, LatLng _location) {
				time = _time;
				location = _location;
		}

		public long getTime() {
				return time;
		}

		public LatLng getLocation() {
				return location;
		}
}