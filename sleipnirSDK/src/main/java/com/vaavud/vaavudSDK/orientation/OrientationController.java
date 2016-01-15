package com.vaavud.vaavudSDK.orientation;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.vaavud.vaavudSDK.VaavudError;
import com.vaavud.vaavudSDK.listener.HeadingListener;

public class OrientationController implements SensorEventListener {

    private HeadingListener headingListener;

    private static final int NUM_ITERATIONS = 40;
    private static final int ACC_DELAY = 1250; // microSeconds
    private static final int MAG_DELAY = 5000; // microSeconds
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor orientationmeter;
    private boolean available;

    private float[] acc = new float[3];
    private float[] mag = new float[3];
    private float[] R = new float[9];
    private float[] Ori = new float[3];
    private float[] I = new float[9];

    private float oriMinValue;
    private int counter = 0;
//    private GeomagneticField mGeomagneticField;
    private float[] orientationAngle = {0, 0, 0};

    private boolean mjolnir;

//    private void updateGeomagneticField() {
//        mGeomagneticField = new GeomagneticField((float) mLocation.getLatitude(),
//                (float) mLocation.getLongitude(), (float) mLocation.getAltitude(),
//                mLocation.getTime());
//    }
//

//    private float computeTrueNorth(float heading) {
//        if (mGeomagneticField != null) {
//            return heading + mGeomagneticField.getDeclination();
//        } else {
//            return heading;
//        }
//    }

    public OrientationController(Context mainContext) {
        sensorManager = (SensorManager) mainContext.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        orientationmeter = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        available = accelerometer != null && magnetometer != null && orientationmeter != null;

        oriMinValue = (float) (Math.PI / 2 - 0.6f);
    }

    public void start() throws VaavudError{
        if (!available) throw new VaavudError("Orientation is not available");

        sensorManager.registerListener(this, accelerometer, ACC_DELAY);
        sensorManager.registerListener(this, magnetometer, MAG_DELAY);
        sensorManager.registerListener(this, orientationmeter, MAG_DELAY);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public boolean isAvailable() {
        return available;
    }

    private void computeOrientation() {

        if (!mjolnir && isVertical()) {
            acc[0] = 0.5F;
            acc[1] = SensorManager.GRAVITY_EARTH;
            acc[2] = 1.2F;
        }

        if (SensorManager.getRotationMatrix(R, I, acc, mag)) {
            SensorManager.getOrientation(R, Ori);
        }

        if (headingListener != null) headingListener.newHeading(getAngle());
    }


    public boolean isVertical() {
        return Math.abs(Ori[1]) > oriMinValue;
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mjolnir) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acc = event.values;
                computeOrientation();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mag = event.values;
            }

        } else {

            float alpha = 0.01F;

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

            if (counter > NUM_ITERATIONS) {
                computeOrientation();
                counter = 1;
            }
        }
    }

    public float getAngle() {
        // TODO Auto-generated method stub
        return orientationAngle[0];
    }

    public float getAngleFromAzimuth() {
        // TODO Auto-generated method stub

        return orientationAngle[0];
    }

//    public float getHeading() {
//        // TODO Auto-generated method stub
//        return mHeading;
//    }
//
//    public double getAcc() {
//        // TODO Auto-generated method stub
//        return acc[1];
//    }

    public void setMjolnir(boolean mjolnir) {
        this.mjolnir = mjolnir;
    }

    public void setHeadingListener(HeadingListener headingListener) {
        this.headingListener = headingListener;
    }
}
