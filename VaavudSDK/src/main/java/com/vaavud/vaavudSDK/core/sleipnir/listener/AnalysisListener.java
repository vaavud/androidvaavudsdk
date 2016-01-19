package com.vaavud.vaavudSDK.core.sleipnir.listener;

public interface AnalysisListener {
	void newRawSignal(short[] signal);
	void volumeChanged(float volumeLevel);
	void newTicks(int[] ticks);
}
