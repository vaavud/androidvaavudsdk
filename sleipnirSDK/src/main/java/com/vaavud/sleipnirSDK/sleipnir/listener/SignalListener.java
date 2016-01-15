package com.vaavud.sleipnirSDK.sleipnir.listener;

public interface SignalListener {

		public void signalChanged(short[] signal);

		public void signalChanged(float[] signal, float[] signalEstimated);

		public void signalChanged(int mvgAvg, int diffFilter, float volumeLevel);

}
