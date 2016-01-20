package com.vaavud.vaavudSDK.core.model.event;

/**
 * Created by juan on 20/01/16.
 */
public class DirectionEvent {
		long time;
		float direction;

		public DirectionEvent() {
		}

		public DirectionEvent(long _time, float _direction) {
				time = _time;
				direction = _direction;
		}

		public long getTime() {
				return time;
		}

		public float getDirection() {
				return direction;
		}
}
