package com.vaavud.sleipnirSDK.wind;

import android.content.Context;

import com.vaavud.sleipnirSDK.listener.SpeedListener;
import com.vaavud.sleipnirSDK.listener.StatusListener;
import com.vaavud.sleipnirSDK.magnetic.FFTManager;
import com.vaavud.sleipnirSDK.magnetic.MagneticDataManager;
import com.vaavud.sleipnirSDK.magnetic.MagneticFieldSensorManager;
import com.vaavud.sleipnirSDK.magnetic.OrientationSensorManager;
import com.vaavud.sleipnirSDK.model.FreqAmp;
import com.vaavud.sleipnirSDK.model.MeasureStatus;
import com.vaavud.sleipnirSDK.model.SpeedEvent;

public class MjolnirWindController implements FrequencyReceiver {

		private static final String TAG = "SDK:MjolnirCore";
		private static final double FREQUENCY_FACTOR = 1.07D;
		private static final double FREQUENCY_START = 0.238D;
		private MeasureStatus status;

		public void setSpeedListener(SpeedListener speedListener) {
				this.speedListener = speedListener;
		}

		public void setStatusListener(StatusListener _statusListener) {
				statusListener = _statusListener;
		}

		private SpeedListener speedListener;
		private StatusListener statusListener;
		private MagneticFieldSensorManager myMagneticFieldSensorManager;
		private OrientationSensorManager orientationSensorManager;

		private MagneticDataManager dataManager;
		private FFTManager myFFTManager;


		public MjolnirWindController(Context context) {

				dataManager = new MagneticDataManager();
				myMagneticFieldSensorManager = new MagneticFieldSensorManager(context, dataManager);
				myFFTManager = new FFTManager(dataManager,this);
				orientationSensorManager = new OrientationSensorManager(context);

		}

		public void startMeasuring() {
				clearData();
				myMagneticFieldSensorManager.startLogging();
				if (orientationSensorManager.isSensorAvailable()) {
						orientationSensorManager.start();
				}
				myFFTManager.start();
				status = MeasureStatus.MEASURING;
		}

		public void stopMeasuring() {
				myMagneticFieldSensorManager.stopLogging();
				if (orientationSensorManager.isSensorAvailable()) {
						orientationSensorManager.stop();
				}
				myFFTManager.stop();
		}

		public void clearData() {
				dataManager.clearData();
				myMagneticFieldSensorManager.clear();
		}

		private void updateMeasureStatus() {

				MeasureStatus newStatus = MeasureStatus.MEASURING;
				if (orientationSensorManager.isSensorAvailable() && !orientationSensorManager.isVertical()) {
						newStatus = MeasureStatus.KEEP_VERTICAL;
				}
				if (!status.equals(newStatus)) {
						statusListener.statusChanged(newStatus);
				}
		}

//		public void stopController() {
//				myMagneticFieldSensorManager = null;
//				orientationSensorManager = null;
//				myFFTManager = null;
//		}


		@Override
		public void newFrequency(FreqAmp data) {
				double windspeed = FREQUENCY_FACTOR * data.frequency + FREQUENCY_START;
				if (data.frequency > 17.65D && data.frequency < 28.87D) {
						windspeed = windspeed + -0.068387D * Math.pow((data.frequency - 23.2667D), 2) + 2.153493D;
				}
				updateMeasureStatus();
				speedListener.speedChanged(new SpeedEvent(data.time, (float) windspeed));
		}
}
