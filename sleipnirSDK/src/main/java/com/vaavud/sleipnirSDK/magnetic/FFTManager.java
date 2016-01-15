package com.vaavud.sleipnirSDK.magnetic;

import android.os.Handler;


import com.vaavud.sleipnirSDK.model.FreqAmp;
import com.vaavud.sleipnirSDK.model.MagneticFieldPoint;
import com.vaavud.sleipnirSDK.wind.FrequencyReceiver;

import java.util.ArrayList;
import java.util.List;

public class FFTManager {

    private static final int LARGEST_FFT_DATA_LENGHT = 100;
    private static final int FFT_LENGHT = 128;
    //	private FFTHandler shortFFT;
    private FFTHandler normalFFT;
//	private FFTHandler longFFT;


    private MagneticDataManager myDataManager;
    private FrequencyReceiver receiver;


    private Handler myHandler = new Handler();
    private boolean isRunning;


    private Runnable calculateFrequency = new Runnable() {
        @Override
        public void run() {
            generateFrequencyAmplitude();
            myHandler.postDelayed(calculateFrequency, 200);
        }
    };

    public FFTManager(MagneticDataManager _myDataManager,FrequencyReceiver _receiver) {
        myDataManager = _myDataManager;
        receiver = _receiver;
        normalFFT = new FFTHandler(LARGEST_FFT_DATA_LENGHT, FFT_LENGHT, FFTHandler.WELCH_WINDOW, FFTHandler.QUADRATIC_INTERPOLATION);
        isRunning = false;
    }



    public void generateFrequencyAmplitude() {

        // get data from dataManager
        List<MagneticFieldPoint> magneticFieldData = myDataManager.getLastXMagneticfieldMeasurements(LARGEST_FFT_DATA_LENGHT);
        FreqAmp myFreqAndAmp;
        // check if enough data is available

        if (magneticFieldData.size() >= normalFFT.getDataLength()) {

            // Prepere data
            List<Float[]> threeAxisData = new ArrayList<Float[]>(normalFFT.getDataLength());
            for (int i = 0; i < magneticFieldData.size(); i++) {
                threeAxisData.add(new Float[]{magneticFieldData.get(i).magneticAxis[0], magneticFieldData.get(i).magneticAxis[1], magneticFieldData.get(i).magneticAxis[2]});
            }

            double timeDiff = magneticFieldData.get(magneticFieldData.size() - 1).time- magneticFieldData.get(0).time;
            double sampleFrequency = (normalFFT.getDataLength() - 1) / timeDiff;

            myFreqAndAmp = normalFFT.getFreqAndAmpThreeAxisFFT(threeAxisData, sampleFrequency);

            if (myFreqAndAmp != null) {
                receiver.newFrequency(myFreqAndAmp);
            }

        } else {
            // do nothing
        }
    }


    public void start() {
        if (!isRunning) {
            myHandler.post(calculateFrequency);
        }
    }

    public void stop() {
        myHandler.removeCallbacks(calculateFrequency);
    }
}
