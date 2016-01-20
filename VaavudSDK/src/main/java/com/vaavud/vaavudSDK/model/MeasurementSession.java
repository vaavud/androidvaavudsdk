package com.vaavud.vaavudSDK.model;

/**
 * Created by juan on 18/01/16.
 */

import com.vaavud.vaavudSDK.core.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasurementSession implements Serializable {

		private String localId;
		private String uuid;
		private String device;
		private boolean measuring = false;
		private boolean uploaded = false;
		private int startIndex = 0;
		private int endIndex = 0;
		private Long timezoneOffset;
		private String geoLocationNameLocalized;
		private Date startTime;
		private Date endTime;
		private LatLng position;
		private Float windSpeedAvg;
		private Float windSpeedMax;
		private Float windDirection;
		private String source;
		private WindMeter windMeter = WindMeter.MJOLNIR;
		private List<MeasurementPoint> points = new ArrayList<MeasurementPoint>();
		private String icon;
		private Float altitude;

		private boolean isItHeader;
		private String currentDate;

		private Float temperature;
		private Integer pressure;
		private Float windChill;
		private Float gustiness;

		public MeasurementSession(String currentDate, boolean isItHeader) {
				this.currentDate = currentDate;
				this.isItHeader = isItHeader;
		}

		public MeasurementSession(String geoLocationNameLocalized, Date startTime, com.vaavud.vaavudSDK.core.model.LatLng position, Float windSpeedAvg, Float windDirection) {
				this.geoLocationNameLocalized = geoLocationNameLocalized;
				this.startTime = startTime;
				this.position = position;
				this.windSpeedAvg = windSpeedAvg;
				this.windDirection = windDirection;
		}

		public MeasurementSession() {
				super();
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

		public boolean isItHeader() {
				return isItHeader;
		}

		public void setIsItHeader(boolean isItHeader) {
				this.isItHeader = isItHeader;
		}

		public String getCurrentDate() {
				return currentDate;
		}

		public void setCurrentDate(String currentDate) {
				this.currentDate = currentDate;
		}

		public String getLocalId() {
				return localId;
		}

		public void setLocalId(String id) {
				this.localId = id;
		}

		public String getUuid() {
				return uuid;
		}

		public void setUuid(String uuid) {
				this.uuid = uuid;
		}

		public String getDevice() {
				return device;
		}

		public void setDevice(String device) {
				this.device = device;
		}

		public int getStartIndex() {
				return startIndex;
		}

		public void setStartIndex(int startIndex) {
				this.startIndex = startIndex;
		}

		public int getEndIndex() {
				return endIndex;
		}

		public void setEndIndex(int endIndex) {
				this.endIndex = endIndex;
		}

		public boolean isMeasuring() {
				return measuring;
		}

		public void setMeasuring(boolean measuring) {
				this.measuring = measuring;
		}

		public boolean isUploaded() {
				return uploaded;
		}

		public void setUploaded(boolean uploaded) {
				this.uploaded = uploaded;
		}

		public Long getTimezoneOffset() {
				return timezoneOffset;
		}

		public void setTimezoneOffset(Long timezoneOffset) {
				this.timezoneOffset = timezoneOffset;
		}

		public Date getStartTime() {
				return startTime;
		}

		public void setStartTime(Date startTime) {
				this.startTime = startTime;
		}

		public Date getEndTime() {
				return endTime;
		}

		public void setEndTime(Date endTime) {
				this.endTime = endTime;
		}

		public com.vaavud.vaavudSDK.core.model.LatLng getPosition() {
				return position;
		}

		public void setPosition(LatLng position) {
				this.position = position;
		}

		public Float getWindSpeedAvg() {
				return windSpeedAvg;
		}

		public void setWindSpeedAvg(Float windSpeedAvg) {
				this.windSpeedAvg = windSpeedAvg;
		}

		public Float getWindSpeedMax() {
				return windSpeedMax;
		}

		public void setWindSpeedMax(Float windSpeedMax) {
				this.windSpeedMax = windSpeedMax;
		}

		public Float getWindDirection() {
				return windDirection;
		}

		public void setWindDirection(Float windDirection) {
				this.windDirection = windDirection;
		}

		public String getSource() {
				return source;
		}

		public void setSource(String source) {
				this.source = source;
		}

		public List<MeasurementPoint> getPoints() {
				return points;
		}

		public void setPoints(List<MeasurementPoint> points) {
				this.points = points;
		}

		public WindMeter getWindMeter() {
				return windMeter;
		}

		public void setWindMeter(WindMeter windMeter) {
				this.windMeter = windMeter;
		}


		public void setIcon(String icon){
				this.icon = icon;
		}

		public String getIcon(){
				return icon;
		}

		@Override
		public boolean equals(Object object) {
				MeasurementSession measurement = (MeasurementSession) object;
				if (!this.isItHeader() && !measurement.isItHeader && this.uuid.compareTo(measurement.getUuid()) == 0) {
//		    	Log.d("MeasurementSession","this.uuid="+this.uuid+" employee.getUuid()="+measurement.getUuid());
						return true;
				}
				return false;
		}

		@Override
		public String toString() {
				return "MeasurementSession [localId=" + localId + ", uuid=" + uuid
								+ ", measuring=" + measuring + ", uploaded=" + uploaded
								+ ", startTime=" + startTime
								+ ", endTime=" + endTime + ", position=" + position
								+ ", windSpeedAvg=" + windSpeedAvg + ", windSpeedMax="
								+ windSpeedMax + ", windDirection=" + windDirection + "]";
		}

}
