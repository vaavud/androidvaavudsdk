package com.vaavud.sleipnirSDK.model;

/**
 * Created by juan on 15/01/16.
 */
public class MagneticFieldPoint {
		public long time;
		public Float[] magneticAxis;

		public MagneticFieldPoint(long _time,Float[] _magneticAxis){
				time = _time;
				magneticAxis = _magneticAxis;
		}
}
