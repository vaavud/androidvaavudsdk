package com.vaavud.vaavudSDK;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.vaavud.vaavudSDK.core.VaavudCoreSDK;
import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.DirectionListener;
import com.vaavud.vaavudSDK.core.listener.HeadingListener;
import com.vaavud.vaavudSDK.core.listener.LocationEventListener;
import com.vaavud.vaavudSDK.core.listener.OrientationListener;
import com.vaavud.vaavudSDK.core.listener.PlugListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.location.LocationService;
import com.vaavud.vaavudSDK.core.model.MeasureStatus;
import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.sleipnir.audio.HeadsetIntentReceiver;
import com.vaavud.vaavudSDK.core.sleipnir.listener.AnalysisListener;
import com.vaavud.vaavudSDK.model.MeasurementSession;
import com.vaavud.vaavudSDK.model.WindMeter;
import com.vaavud.vaavudSDK.model.event.BearingEvent;
import com.vaavud.vaavudSDK.model.event.TrueDirectionEvent;
import com.vaavud.vaavudSDK.model.event.TrueSpeedEvent;
import com.vaavud.vaavudSDK.model.event.VelocityEvent;

import java.util.Date;
import java.util.Map;


/**
 * Created by juan on 18/01/16.
 */
public class VaavudSDK implements SpeedListener, DirectionListener, LocationEventListener, OrientationListener, StatusListener, PlugListener {

		private static final String TAG = "VaavudSDK";
		private static final long TIME_THRESHOLD = 1000;
		private final HeadsetIntentReceiver receiver;
		private Context context;
		private VaavudCoreSDK sdk;
		private MeasurementSession session;
		private Config config;
		private LocationService _location;

		private SpeedListener vaavudSpeed;
		private DirectionListener vaavudDirection;
		private LocationEventListener vaavudLocation;
		private OrientationListener vaavudOrientation;


		private boolean sleipnirAvailable = false;


		public VaavudSDK(Context _context, Map<String, Object> configuration) {
				context = _context;
				if (sdk == null) {
						sdk = new VaavudCoreSDK(context);
				}
				IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
				receiver = new HeadsetIntentReceiver(this);
				context.registerReceiver(receiver, receiverFilter);
				config = new Config(configuration);
		}

		public boolean isSleipnirAvailable() {
				return sleipnirAvailable;
		}

		public MeasurementSession startSession() throws VaavudError {
				session = new MeasurementSession();
				session.startSession();
				location().setEventListener(this);
				location().start();
				if (config.getWindMeter().equals(WindMeter.SLEIPNIR) & sleipnirAvailable)
						sdk.startSleipnir();
				if (!sleipnirAvailable || config.getWindMeter().equals(WindMeter.MJOLNIR) ) sdk.startMjolnir();
				return session;

		}

		public MeasurementSession stopSession() throws VaavudError {
			location().stop();
			if (config.getWindMeter().equals(WindMeter.SLEIPNIR) & sleipnirAvailable){
				if (isRunning()) {
					sdk.stopSleipnir();
				}
			}else{
				sdk.stopMjolnir();
			}
			return session.stopSession();
		}

		private LocationService location() {
				if (_location == null) {
						_location = new LocationService(context, config.getLocationFrequency());
				}
				return _location;
		}

		public boolean isRunning() {
				return sdk.isSleipnirActive();
		}


		// // FIXME: 21/01/16 concider removing and ask users to access to the SDKCore directly
//
		public void setSpeedListener(SpeedListener speedListener) {
				sdk.setSpeedListener(this);
				vaavudSpeed = speedListener;
		}

		public void setDirectionListener(DirectionListener directionListener) {
				sdk.setDirectionListener(this);
				vaavudDirection = directionListener;
		}

		public void setLocationListener(LocationEventListener locationListener) {
				vaavudLocation = locationListener;

		}

		public void setOrientationListener(OrientationListener orientationListener) {
				sdk.setOrientationListener(this);
				vaavudOrientation = orientationListener;
		}

		public void setHeadingListener(HeadingListener headingListener) {
				sdk.setHeadingListener(headingListener);
		}

		public void setAnalysisListener(AnalysisListener analysisListener) {
				sdk.setAnalysisListener(analysisListener);
		}

		public VaavudCoreSDK getSdk() {
				return sdk;
		}

		@Override
		public void speedChanged(SpeedEvent event) {
				session.addSpeedEvent(event);
//				float windSpeed = session.getWindMean();
				vaavudSpeed.speedChanged(new SpeedEvent(event.getTime(), event.getSpeed()));
				if (!estimateTrueWind()) {
						TrueSpeedEvent newEvent = new TrueSpeedEvent(event.getTime(), event.getSpeed());
						session.addTrueSpeedEvent(newEvent);
						vaavudSpeed.trueSpeedChanged(newEvent);
				}
		}

		@Override
		public void trueSpeedChanged(TrueSpeedEvent event) {
		}

		@Override
		public void newDirectionEvent(DirectionEvent event) {
				session.addDirectionEvent(event);
				vaavudDirection.newDirectionEvent(event);
				if (!estimateTrueWind()) {
						TrueDirectionEvent newEvent = new TrueDirectionEvent(event.getTime(), event.getDirection());
						session.addTrueDirectionEvent(newEvent);
						vaavudDirection.trueDirectionEvent(newEvent);
				}
		}

		@Override
		public void trueDirectionEvent(TrueDirectionEvent event) {
		}

		@Override
		public void statusChanged(MeasureStatus status) {
		}

		@Override
		public void newLocation(LocationEvent event) {
//        Log.d(TAG, "New Location: " + event.getLocation());
				session.addLocationEvent(event);
				if (vaavudLocation != null)
						vaavudLocation.newLocation(event);
		}

		@Override
		public void newVelocity(VelocityEvent event) {
//        Log.d(TAG, "New Velocity: " + event.getVelocity());
				session.addVelocityEvent(event);
				if (vaavudLocation != null)
						vaavudLocation.newVelocity(event);
		}

		@Override
		public void newBearing(BearingEvent event) {
				if (event.getBearing() != 0.0f) {
						session.addBearingEvent(event);
						if (vaavudLocation != null)
								vaavudLocation.newBearing(event);
				}
		}

		private boolean estimateTrueWind() {
				VelocityEvent velocity = session.getLastVelocityEvent();
				DirectionEvent direction = session.getLastDirectionEvent();
				SpeedEvent speed = session.getLastSpeedEvent();
				BearingEvent bearing = session.getLastBearingEvent();

				if (direction == null || speed == null || bearing == null || velocity == null) {
						return false;
				}
				long time = new Date().getTime();
				if ((time - direction.getTime()) > TIME_THRESHOLD || (time - speed.getTime()) > TIME_THRESHOLD
								|| (time - bearing.getTime()) > TIME_THRESHOLD || (time - velocity.getTime()) > TIME_THRESHOLD) {
						return false;
				}

				float alpha = direction.getDirection() - bearing.getBearing();
				double rad = Math.toRadians(alpha);
				float trueSpeed = (float) Math.sqrt(Math.pow(speed.getSpeed(), 2.0) + Math.pow(velocity.getVelocity(), 2) - 2 * speed.getSpeed() * velocity.getVelocity() * Math.cos(rad));

				TrueSpeedEvent speedEvent = null;
				if (trueSpeed >= 0) {
						speedEvent = new TrueSpeedEvent(new Date().getTime(), trueSpeed);
				}

				float trueDirection = -1;
				if (0 < rad && Math.PI > rad) {
						trueDirection = (float) Math.acos((speed.getSpeed() * Math.cos(rad) - velocity.getVelocity()) / trueSpeed);
				} else {
						trueDirection = (-1) * (float) Math.acos((speed.getSpeed() * Math.cos(rad) - velocity.getVelocity()) / trueSpeed);
				}
				trueDirection = (float) Math.toDegrees(trueDirection);

				TrueDirectionEvent directionEvent = null;
				if (trueDirection != -1) {
						directionEvent = new TrueDirectionEvent(new Date().getTime(), trueDirection);
				}

				if (speedEvent != null && directionEvent != null) {
						session.addTrueSpeedEvent(speedEvent);
						session.addTrueDirectionEvent(directionEvent);
						vaavudSpeed.trueSpeedChanged(speedEvent);
						vaavudDirection.trueDirectionEvent(directionEvent);
						return true;
				}
				return false;
		}

		@Override
		public void finalize() {
				context.unregisterReceiver(receiver);
		}

		@Override
		public void permisionError(String permission) {
//				Log.d(TAG, "Permission Error: " + permission);
		}

		@Override
		public void newOrientation(float x, float y, float z) {
		}

		@Override
		public void onHeadsetStatusChanged(boolean plugged) {
				sleipnirAvailable = plugged;
		}

		@Override
		public void isSleipnirPlugged(boolean plugged) {

		}

}
