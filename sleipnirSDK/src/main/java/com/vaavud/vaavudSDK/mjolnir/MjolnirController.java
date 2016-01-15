package com.vaavud.vaavudSDK.mjolnir;

import android.content.Context;

import com.vaavud.vaavudSDK.listener.SpeedListener;
import com.vaavud.vaavudSDK.listener.StatusListener;
import com.vaavud.vaavudSDK.model.FreqAmp;
import com.vaavud.vaavudSDK.model.MeasureStatus;
import com.vaavud.vaavudSDK.model.SpeedEvent;

public class MjolnirController implements FrequencyReceiver {

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


		public MjolnirController(Context context) {

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
