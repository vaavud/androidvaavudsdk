package com.vaavud.vaavudSDK.model;

/**
 * Created by juan on 18/01/16.
 */

import com.vaavud.vaavudSDK.core.model.event.DirectionEvent;
import com.vaavud.vaavudSDK.core.model.event.LocationEvent;
import com.vaavud.vaavudSDK.core.model.event.SpeedEvent;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MeasurementSession implements Serializable {

		private String geoLocationNameLocalized;
		private Date startTime;
		private Date endTime;
		private List<LocationEvent> location;
		private List<SpeedEvent> speed;
		private List<DirectionEvent> direction;
		private WindMeter windMeter = WindMeter.MJOLNIR;

		private Float altitude;

		private Float temperature;
		private Integer pressure;
		private Float windChill;
		private Float gustiness;


		public MeasurementSession() {
				super();
		}

		public void startSession(){
				speed.clear();
				location.clear();
				direction.clear();
				startTime = new Date();
		}

		public void addSpeedEvent(SpeedEvent event){
				speed.add(event);
		}
		public void addLocationEvent(LocationEvent event){
				location.add(event);
		}
		public void addDirectionEvent(DirectionEvent event){
				direction.add(event);
		}

		public MeasurementSession stopSession(){
				endTime = new Date();
				return this;
		}

		public int getNumSpeedEvents(){
				return speed.size();
		}

		public void setAltitude(Float altitude){
				this.altitude = altitude;
		}

		public Float getAltitude(){
				return altitude;
		}

		public void setTemperature(Float temperature){
				this.temperature=temperature;
		}

		public Float getTemperature(){
				return temperature;
		}

		public void setPressure(Integer pressure){
				this.pressure=pressure;
		}

		public Integer getPressure(){
				return pressure;
		}

		public void setWindChill(Float windChill){
				this.windChill=windChill;
		}

		public Float getWindChill(){
				return windChill;
		}

		public void setGustiness(Float gustiness){
				this.gustiness=gustiness;
		}

		public Float getGustiness(){
				return gustiness;
		}

		public String getGeoLocationNameLocalized() {
				return geoLocationNameLocalized;
		}

		public void setGeoLocationNameLocalized(String geoLocationNameLocalized) {
				this.geoLocationNameLocalized = geoLocationNameLocalized;
		}

		public Date getStartTime() {
				return startTime;
		}

		public Date getEndTime() {
				return endTime;
		}

		public WindMeter getWindMeter() {
				return windMeter;
		}

		public void setWindMeter(WindMeter windMeter) {
				this.windMeter = windMeter;
		}


}
