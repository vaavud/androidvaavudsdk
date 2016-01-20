package com.vaavud.vaavudSDK.core.model.event;

/**
 * Created by juan on 19/01/16.
 */
public class HeadingEvent {
		long time;
		float heading;

		public HeadingEvent() {
		}

		public HeadingEvent(long _time, float _heading) {
				time = _time;
				heading = _heading;
		}

		public long getTime() {
				return time;
		}

		public float getHeading() {
				return heading;
		}
}