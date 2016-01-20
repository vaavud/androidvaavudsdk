package com.vaavud.vaavudSDK.core.sleipnir.listener;

import com.vaavud.vaavudSDK.core.sleipnir.model.Rotation;

public interface AnalysisListener {
	void newRawSignal(short[] signal);
	void volumeChanged(float volumeLevel);
	void newTicks(int[] ticks);
	void newRotation(Rotation rotation);
}
