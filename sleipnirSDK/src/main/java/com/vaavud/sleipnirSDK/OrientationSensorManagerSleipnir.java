package com.vaavud.sleipnirSDK;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

public class OrientationSensorManagerSleipnir implements SensorEventListener {

    private static final int NUM_ITERATIONS = 40;
    private static final int ACC_DELAY = 1250; // microSeconds
    private static final int MAG_DELAY = 5000; // microSeconds
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticSensor;
    private Sensor orientationSensor;
    private boolean sensorAvailable;

    private float[] acc = new float[3];
    private float[] mag = new float[3];
    private float[] R = new float[9];
    private float[] Ori = new float[3];
    private float[] I = new float[9];

    //	private short[] bufferAcc = new short[357];
    private float oriMinValue;
    private int counter = 0;
    private Location mLocation;
    private GeomagneticField mGeomagneticField;
    private float mHeading;
    private float[] orientationAngle = {0, 0, 0};

    private void updateGeomagneticField() {
        mGeomagneticField = new GeomagneticField((float) mLocation.getLatitude(),
                (float) mLocation.getLongitude(), (float) mLocation.getAltitude(),
                mLocation.getTime());
    }


    private float computeTrueNorth(float heading) {
        if (mGeomagneticField != null) {
            return heading + mGeomagneticField.getDeclination();
        } else {
            return heading;
        }
    }


    public OrientationSensorManagerSleipnir(Context mainContext) {
        sensorManager = (SensorManager) mainContext.getSystemService(Context.SENSOR_SERVICE);
//		locationManager = (LocationManager) mainContext.getSystemService(Context.LOCATION_SERVICE);
//		mSignalListener = listener;
        initializeOrientation();
        if (initializeOrientation()) {
            sensorAvailable = true;
        } else {
            sensorAvailable = false;
        }

        this.oriMinValue = (float) (Math.PI / 2 - 0.6f);
        // Define a listener that responds to location updates
//		locationListener = new LocationListener() {
//			@Override
//			public void onLocationChanged(Location location){
//				Log.d("Orientation","OnLocationChanged");
//				mLocation = location;
//		        updateGeomagneticField();
//			}
//
//		    public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//		    public void onProviderEnabled(String provider) {}
//
//		    public void onProviderDisabled(String provider) {}
//		  };


    }

    private boolean initializeOrientation() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (accelerometer != null && magneticSensor != null) {
            return true;
        } else {
            return false;
        }

    }

    public void start() {
        if (sensorAvailable) {
            sensorManager.registerListener(this, accelerometer, ACC_DELAY);
            sensorManager.registerListener(this, magneticSensor, MAG_DELAY);
            sensorManager.registerListener(this, orientationSensor, MAG_DELAY);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public boolean isSensorAvailable() {
        return sensorAvailable;
    }


    private void computeOrientation() {
        if (isVertical()) {
//			Log.d("OrientationManager","Is Vertical");
            acc[0] = 0.5F;
            acc[1] = SensorManager.GRAVITY_EARTH;
            acc[2] = 1.2F;
        }

        if (SensorManager.getRotationMatrix(R, I, acc, mag)) {
            SensorManager.getOrientation(R, Ori);
        }
    }


    public Boolean isVertical() {
        if (!sensorAvailable) {
            return null;
        }

        if (Math.abs(Ori[1]) > oriMinValue) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float alpha = 0.01F;
        if (counter > NUM_ITERATIONS) {
            computeOrientation();
            counter = 1;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc[0] = alpha * event.values[0] + (1 - alpha) * acc[0];
            acc[1] = alpha * event.values[1] + (1 - alpha) * acc[1];
            acc[2] = alpha * event.values[2] + (1 - alpha) * acc[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag[0] = (counter * mag[0] + event.values[0]) / (counter + 1);
            mag[1] = (counter * mag[1] + event.values[1]) / (counter + 1);
            mag[2] = (counter * mag[2] + event.values[2]) / (counter + 1);
            counter++;
        }


        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            orientationAngle = event.values.clone();
        }
    }

    public double getAngle() {
        // TODO Auto-generated method stub
        return orientationAngle[0];
    }

    public double getAngleFromAzimuth() {
        // TODO Auto-generated method stub

        return orientationAngle[0];
    }

    public float getHeading() {
        // TODO Auto-generated method stub
        return mHeading;
    }

    public double getAcc() {
        // TODO Auto-generated method stub
        return acc[1];
    }


}
