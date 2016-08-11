package com.vaavud.vaavudSDK;

import com.vaavud.vaavudSDK.model.WindMeter;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by juan on 02/02/16.
 */
public class Config {

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
                    setWindMeter((WindMeter) entry.getValue());
                    break;
                case "updateFrequency":
                    setUpdateFrequency((int) entry.getValue());
                    break;
                case "locationFrequency":
                    setUpdateFrequency((int) entry.getValue());
                    break;
                default:
                    break;
            }

        }
    }
}