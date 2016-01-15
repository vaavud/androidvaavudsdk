package com.vaavud.vaavudSDK.sleipnir.audio;

//import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by juan on 16/07/15.
 */
public class VolumeAdjust {

    private static final String TAG = "SDK:VolumeAdjust";

    private static final int DIFF_STATE = 0;
    private static final int SEQUENTIALSEARCH_STATE = 1;
    private static final int STEEPESTASCENT_STATE = 2;

    private static final int TOP_STATE = 0;
    private static final int EXPLORE_STATE = 1;

    private static final int LEFT_STATE = 0;
    private static final int RIGHT_STATE = 1;


    private static final int VOLUME_STEPS = 100;
    private static final int NOISE_THRESHOLD = 1100;

    private short[] buffer;
    private double[] sNVolume = new double[VOLUME_STEPS + 1];

    private int volumeCounter = 0;

    private int volState = 0;
    private int expState = 0;
    private int dirState = 0;

    private int analysisPeriod = 512*20;
    private int skipSamples = 0;
    private int nSamples = 100;
    private long counter = 0;
    private List<Integer> diffValues;
    private int samplesPerBuffer = 100;
    private int volumeLevel = VOLUME_STEPS / 2;

    private boolean rotationDetected = false;

    public VolumeAdjust(int processBufferSize, int audioBufferSize, Float playerVolume) {
        diffValues = new ArrayList<>();
        volumeLevel = (int) (playerVolume * 100);
        if (volumeLevel != 50) {
            volState = STEEPESTASCENT_STATE;
        }
        buffer = new short[processBufferSize];
        skipSamples = audioBufferSize * 2;
        samplesPerBuffer = processBufferSize*nSamples/analysisPeriod;
    }

    public Float newVolume(short[] audioBuffer) {
        counter += audioBuffer.length;
        if (counter <= skipSamples) {
            return null;
        }

        System.arraycopy(audioBuffer, 0, buffer, 0, buffer.length); // FIXME: 14/01/16 is this necessary?
        for (int i = 0; (i < samplesPerBuffer) && (diffValues.size() < nSamples); i++) {
            int index = (int) (i / (float) samplesPerBuffer * buffer.length);

            int diff = 0;
            for (int j = 0; j < 3; j++) {
                diff = diff + Math.abs(buffer[index + j] - buffer[index + j + 1]);
            }
            diffValues.add(diff);
        }

        if (diffValues.size() == nSamples) {
            Collections.sort(diffValues);
            int diff20 = diffValues.get(19);
            double sN = (double) diffValues.get(79) / (double) diffValues.get(39);

            float vol = newVolumeX(diff20, sN);
            counter = 0;
            rotationDetected = false;
            diffValues.clear();

            return vol;
        }

        return null;
    }

    public float newVolumeX(int diff20, double sN) {
        volumeCounter++;
        float volumeChange = 0.0f;
        if (sN > 6 && rotationDetected) {
            volState = STEEPESTASCENT_STATE;
        }
        switch (volState) {
            case DIFF_STATE:
                float noiseDiff = Math.abs(diff20 - NOISE_THRESHOLD);

                if (diff20 >= NOISE_THRESHOLD) {
                    volumeChange = (float) VOLUME_STEPS * (-noiseDiff) / 50000;
                }
                if (diff20 < NOISE_THRESHOLD) {
                    volumeChange = (float) VOLUME_STEPS * noiseDiff / 10000;
                }
                volumeLevel = (int) (volumeLevel + volumeChange);
                if (volumeCounter > 15) {
                    volState = SEQUENTIALSEARCH_STATE;
                }
                break;

            case SEQUENTIALSEARCH_STATE:
                if (volumeCounter > 45) {
                    returnToDiffState();
                    break;
                }
                volumeLevel = (int) (volumeCounter % 20 * (VOLUME_STEPS / 20.0f) + VOLUME_STEPS / 40.0f); // 5, 15, 25 ... 95
                break;

            case STEEPESTASCENT_STATE:
                boolean signalIsGood = (sN > 1.2 && rotationDetected);
                if (signalIsGood) {
                    sNVolume[volumeLevel] = ((sNVolume[volumeLevel] == 0.0d) ? sN : sNVolume[volumeLevel] * 0.7 + 0.3 * sN);
                    volumeCounter = 0;
                } else {
                    if (volumeCounter > 40) {
                        returnToDiffState();
                        break;
                    }
                }
                switch (expState) {
                    case TOP_STATE:
                        int bestSNVol = bestSNVolume();
                        if (sNVolume[bestSNVol] < 6) {
                            returnToDiffState();
                            break;
                        }

                        volumeChange = bestSNVol - volumeLevel;
                        volumeChange = (volumeChange >= 1 && volumeChange < 5) ? 1 : (volumeChange <= -1 && volumeChange > -5) ? -1 : volumeChange;
                        volumeLevel = (int) (volumeLevel + volumeChange);
                        if (volumeChange == 0) {
                            expState = EXPLORE_STATE;
                        }
                        break;

                    case EXPLORE_STATE:
                        switch (dirState) {
                            case LEFT_STATE:
                                volumeLevel = volumeLevel - 1;
                                dirState = RIGHT_STATE;
                                break;
                            case RIGHT_STATE:
                                volumeLevel = volumeLevel + 1;
                                dirState = LEFT_STATE;
                                break;
                        }
                        expState = TOP_STATE;
                        break;
                }
        }

        if (volumeLevel < 0) {
            volumeLevel = 0;
        } else if (volumeLevel > VOLUME_STEPS - 1) {
            volumeLevel = VOLUME_STEPS - 1;
        }
        return getVolume();
    }

    public float getVolume() {
        return (float) volumeLevel / (float) VOLUME_STEPS;
    }

    private void returnToDiffState() {
        volState = DIFF_STATE;
        counter = 0;
        sNVolume = new double[VOLUME_STEPS];
    }

    public int bestSNVolume() {
        double max = 0.0f;
        int maxi = 0;
        for (int i = 0; i < sNVolume.length; i++) {
            if (sNVolume[i] > max) {
                maxi = i;
                max = sNVolume[i];
            }
        }
        return maxi;
    }

    public void newRotation() {
        if (counter > skipSamples) rotationDetected = true;
    }
}