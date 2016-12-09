package com.vaavud.vaavudSDK.core.mjolnir;


import android.util.Log;

import com.vaavud.vaavudSDK.core.model.MagneticFieldPoint;

import java.util.ArrayList;
import java.util.List;


public class MagneticDataManager {

    private List<MagneticFieldPoint> magneticfieldMeasurements;
    private int lastServedWindmeasurement = 0;


    public MagneticDataManager() {
        magneticfieldMeasurements = new ArrayList<>();
    }

    public void addMagneticFieldReading(MagneticFieldPoint magReading) {
        synchronized (magneticfieldMeasurements) {
            magneticfieldMeasurements.add(magReading);
//            Log.d("MagneticDataManager", "Size of: " + magneticfieldMeasurements.size());
        }
    }

    public List<MagneticFieldPoint> getLastXMagneticfieldMeasurements(Integer numberOfMeasurements) {
        synchronized (magneticfieldMeasurements) {
            int listSize = magneticfieldMeasurements.size();
            if (listSize > numberOfMeasurements) {
                List<MagneticFieldPoint> lastPoints = new ArrayList<>(magneticfieldMeasurements.subList(listSize - numberOfMeasurements, listSize));
                return lastPoints;
            } else {
                return new ArrayList<>(magneticfieldMeasurements);
            }
        }
    }

    public List<MagneticFieldPoint> getMagneticfieldMeasurements() {
        return magneticfieldMeasurements;
    }

    public void clearData() {
        magneticfieldMeasurements = new ArrayList<>();
    }

    public boolean newMeasurementsAvailable() {
        return magneticfieldMeasurements.size() > lastServedWindmeasurement;
    }

}
