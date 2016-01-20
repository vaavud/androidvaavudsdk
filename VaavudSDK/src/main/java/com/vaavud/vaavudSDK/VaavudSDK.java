package com.vaavud.vaavudSDK;


import android.content.Context;
import android.util.Log;

import com.vaavud.vaavudSDK.core.VaavudCoreSDK;
import com.vaavud.vaavudSDK.core.VaavudError;
import com.vaavud.vaavudSDK.core.listener.DirectionListener;
import com.vaavud.vaavudSDK.core.listener.LocationEventListener;
import com.vaavud.vaavudSDK.core.listener.SpeedListener;
import com.vaavud.vaavudSDK.core.listener.StatusListener;
import com.vaavud.vaavudSDK.core.location.LocationService;
import com.vaavud.vaavudSDK.core.model.MeasureStatus;
import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;
import com.vaavud.vaavudSDK.core.model.event.VelocityEvent;
import com.vaavud.vaavudSDK.model.MeasurementSession;
import com.vaavud.vaavudSDK.model.WindMeter;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by juan on 18/01/16.
 */
public class VaavudSDK implements SpeedListener, DirectionListener, LocationEventListener, StatusListener {

		private static final String TAG = "VaavudSDK";
		private Context context;
		private VaavudCoreSDK sdk;
		private MeasurementSession session;
		private Config config;
		private LocationService _location;

		private Float windSpeedAvg;
		private Float windSpeedMax;
		private Float windDirection;



		public VaavudSDK(Context _context, Map<String, Object> configuration) {
				context = _context;
				if (sdk == null) {
						sdk = new VaavudCoreSDK(context);
				}
				config = new Config(configuration);

		}

		public void startSession() throws VaavudError {

				session = new MeasurementSession();
				session.startSession();
				location().setEventListener(this);
				location().start();

				if (config.getWindMeter().equals(WindMeter.SLEIPNIR)) sdk.startSleipnir();
				else sdk.startMjolnir();

		}

		public void stopSession() throws VaavudError {

//				session = new MeasurementSession();
				session.stopSession();
				location().stop();
				if (config.getWindMeter().equals(WindMeter.SLEIPNIR)) sdk.stopSleipnir();
				else sdk.stopMjolnir();

		}

		private LocationService location() {
				if (_location == null) {
						_location = new LocationService(context, config.getLocationFrequency());
				}
				return _location;
		}

		public boolean isRunning(){
				return sdk.isSleipnirActive();
		}

		public void setSpeedListener(SpeedListener speedListener){
				sdk.setSpeedListener(speedListener);
		}


		@Override
		public void speedChanged(SpeedEvent event) {
				if (event.getSpeed() > windSpeedMax) windSpeedMax = event.getSpeed();
				windSpeedAvg = (event.getSpeed() + session.getNumSpeedEvents()*windSpeedAvg)/(session.getNumSpeedEvents()+1);
				session.addSpeedEvent(event);
		}

		@Override
		public void newDirectionEvent(DirectionEvent event) {
				session.addDirectionEvent(event);
		}

		@Override
		public void statusChanged(MeasureStatus status) {

		}

		@Override
		public void newLocation(LocationEvent event) {
				Log.d(TAG, "New Location: " + event.getLocation());
				session.addLocationEvent(event);
		}

		@Override
		public void newVelocity(VelocityEvent event) {
				Log.d(TAG, "New Velocity: " + event.getVelocity());

		}

		@Override
		public void permisionError(String permission) {
				Log.d(TAG, "Permission Error: "+permission);
		}




		class Config {
				private WindMeter windMeter;
				private int updateFrequency;
				private long locationFrequency;

				public Config(Map<String, Object> configuration) {
						if (configuration != null) {
								configure(configuration);
						} else {
								windMeter = WindMeter.SLEIPNIR;
								updateFrequency = 200;
								locationFrequency = 1000;
						}

				}

				public WindMeter getWindMeter() {
						return windMeter;
				}

				public void setWindMeter(WindMeter _windMeter) {
						windMeter = _windMeter;
				}

				public int getUpdateFrequency() {
						return updateFrequency;
				}

				public void setUpdateFrequency(int _updateFrequency) {
						updateFrequency = _updateFrequency;
				}

				public long getLocationFrequency() {
						return locationFrequency;
				}

				public void setLocationFrequency(int _locationFrequency) {
						locationFrequency = _locationFrequency;
				}

				private void configure(Map<String, Object> configuration) {
						Iterator<Map.Entry<String, Object>> it = configuration.entrySet().iterator();
						while (it.hasNext()) {
								Map.Entry<String, Object> entry = it.next();
								switch (entry.getKey()) {
										case "windMeter":
												config.setWindMeter((WindMeter) entry.getValue());
												break;
										case "updateFrequency":
												config.setUpdateFrequency((int) entry.getValue());
												break;
										case "locationFrequency":
												config.setUpdateFrequency((int) entry.getValue());
												break;
										default:
												break;
								}

						}
				}
		}
}
