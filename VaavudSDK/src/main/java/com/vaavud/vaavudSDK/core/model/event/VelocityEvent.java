package com.vaavud.vaavudSDK.core.model.event;

/**
 * Created by juan on 19/01/16.
 */
public class VelocityEvent {
		long time;
		float velocity;

		public VelocityEvent() {
		}

		public VelocityEvent(long _time, float _velocity) {
				time = _time;
				velocity = _velocity;
		}

		public long getTime() {
				return time;
		}

		public float getVelocity() {
				return velocity;
		}
}