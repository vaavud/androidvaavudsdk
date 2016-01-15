package com.vaavud.vaavudSDK.mjolnir;


import com.vaavud.vaavudSDK.model.MagneticFieldPoint;

import java.util.ArrayList;
import java.util.List;


public class MagneticDataManager {

    private List<MagneticFieldPoint> magneticfieldMeasurements;
    private int lastServedWindmeasurement = 0;


    public MagneticDataManager() {
        magneticfieldMeasurements = new ArrayList<>();
    }

    public void addMagneticFieldReading(MagneticFieldPoint magReading) {
        magneticfieldMeasurements.add(magReading);
    }

    public List<MagneticFieldPoint> getLastXMagneticfieldMeasurements(Integer numberOfMeasurements) {

        int listSize = magneticfieldMeasurements.size();
        List<MagneticFieldPoint> magneticfieldMeasurementsList;

        if (listSize > numberOfMeasurements) {
            magneticfieldMeasurementsList = magneticfieldMeasurements.subList(listSize - numberOfMeasurements, listSize);

            return magneticfieldMeasurementsList;
        } else {

            magneticfieldMeasurementsList = new ArrayList<>();
            magneticfieldMeasurementsList.addAll(magneticfieldMeasurements);

            return magneticfieldMeasurementsList;
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
