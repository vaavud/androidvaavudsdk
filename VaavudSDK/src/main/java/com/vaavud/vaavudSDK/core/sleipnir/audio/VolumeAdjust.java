package com.vaavud.vaavudSDK.core.sleipnir.audio;

//import android.util.Log;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by juan on 16/07/15.
 */
public class VolumeAdjust {

    private static final String TAG = "SDK:VolumeAdjust";
    SharedPreferences sharedPreferences;
    private final String KEY_VOLUME = "volume";
    private final String KEY_SN_VOLUME = "sQvolume";

    private static final int DIFF_STATE = 0;
    private static final int SEQUENTIALSEARCH_STATE = 1;
    private static final int STEEPESTASCENT_STATE = 2;

    private static final int TOP_STATE = 0;
    private static final int EXPLORE_STATE = 1;

    private static final int LEFT_STATE = 0;
    private static final int RIGHT_STATE = 1;


    private static final int VOLUME_STEPS = 100;
    private static final float NOISE_THRESHOLD = 0.017f;

    private short[] buffer;
    public float[] sQVolume; // for debugging

    private int volumeCounter = 0;

    private int volState;
    private int expState;
    private int dirState;

    private int analysisPeriod = 512*20;
    private int skipSamples = 0;
    private int nSamples = 100;
    private long counter = 0;
    private List<Integer> diffValues = new ArrayList<>();
    private List<Integer> rawValues = new ArrayList<>();
    private int samplesPerBuffer = 100;
    private int volumeLevel;

    private boolean rotationDetected = false;

    public VolumeAdjust(int processBufferSize, int audioBufferSize, SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;

        Float vol = sharedPreferences.getFloat(KEY_VOLUME, -1);
        if (vol != -1) {
            volState = STEEPESTASCENT_STATE;
        } else {
            volState = DIFF_STATE;
            vol = 0.5f;
        }

        volumeLevel = (int) (vol * 100);

        sQVolume = loadArray(sharedPreferences, KEY_SN_VOLUME, new float[VOLUME_STEPS + 1]);

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
            rawValues.add((int) buffer[index]);
        }

        if (diffValues.size() == nSamples) {
            Collections.sort(diffValues);
            Collections.sort(rawValues);
            float diff20 = diffValues.get(19) / 65536f;
            float sR = (float) (rawValues.get(95) - rawValues.get(5)) / 65536f;
            float sN = (float) diffValues.get(79) / (float) diffValues.get(39); // signal to noise times absolute max value
            float sQ = sN*sR;

            float vol = newVolumeX(diff20, sR, sN, sQ);
            counter = 0;
            rotationDetected = false;
            diffValues.clear();
            rawValues.clear();

            return vol;
        }
        return null;
    }

    public float newVolumeX(float diff20, float sR, float sN, float sQ) {

        sQVolume[volumeLevel] = sQ;
        volumeCounter++;

        float volumeChange = 0f;

        switch (volState) {
            case DIFF_STATE:
//                Log.d(TAG, "DIFF_STATE");
                float noiseDiff = Math.abs(diff20 - NOISE_THRESHOLD);

                if (diff20 >= NOISE_THRESHOLD) volumeChange = (float) VOLUME_STEPS * -noiseDiff;
                if (diff20 < NOISE_THRESHOLD) volumeChange = (float) VOLUME_STEPS * noiseDiff * 2;
                volumeChange = Math.abs(volumeChange) < 10 ? volumeChange : volumeChange > 0 ? 8 : -10;
                volumeLevel = (int) (volumeLevel + volumeChange);
                if (volumeCounter > 15) volState = SEQUENTIALSEARCH_STATE;
                if (sQ > 6.0 && rotationDetected) volState = STEEPESTASCENT_STATE;

                break;

            case SEQUENTIALSEARCH_STATE:
//                Log.d(TAG, "SEQUENTIALSEARCH_STATE");
                volumeLevel = (int) (volumeCounter % 20 * (VOLUME_STEPS / 20.0f) + VOLUME_STEPS / 40.0f); // 5, 15, 25 ... 95
                if (volumeCounter > 45) returnToDiffState();
                if (sQ > 6.0 && rotationDetected) volState = STEEPESTASCENT_STATE;

                break;

            case STEEPESTASCENT_STATE:
//                Log.d(TAG, "STEEPESTASCENT_STATE");
                boolean signalIsGood = (sQ > 1.2 && rotationDetected);
                if (signalIsGood) {
//                    sQVolume[volumeLevel] = sQ; //sQVolume[volumeLevel] == 0.0f ? sQ : sQVolume[volumeLevel] * 0.7f + sQ * 0.3f;
                    volumeCounter -= 2;
                    if (volumeCounter < 0) volumeCounter = 0;
                }

                if (sR > 0.6 && diff20 < 0.3 && sN < 10 || volumeCounter > 40) {
                    returnToDiffState();
                    break;
                }
                switch (expState) {
                    case TOP_STATE:
                        int bestSNVol = bestSNVolume();
                        if (sQVolume[bestSNVol] < 10) {
                            returnToDiffState();
                            break;
                        }
                        int volChange = bestSNVol - volumeLevel;
                        if (volChange == 0) expState = EXPLORE_STATE;
                        volChange =
                                (volChange >= 1 && volChange < 5) ? 1 : (volChange <= -1 && volChange > -5) ? -1 : volChange;
                        volumeLevel = (volumeLevel + volChange);
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
        } else if (volumeLevel > VOLUME_STEPS) {
            volumeLevel = VOLUME_STEPS;
        }
        return getVolume();
    }

    public float getVolume() {
        return (float) volumeLevel / (float) VOLUME_STEPS;
    }

    private void returnToDiffState() {
        volState = DIFF_STATE;
        volumeCounter = 0;
//        sQVolume = new float[VOLUME_STEPS];
    }

    public int bestSNVolume() {
        float max = 0.0f;
        int maxi = 0;
        for (int i = 0; i < sQVolume.length; i++) {
            if (sQVolume[i] > max) {
                maxi = i;
                max = sQVolume[i];
            }
        }
        return maxi;
    }


    private float[] loadArray(SharedPreferences pref, String key, float[] array) {
        String json = pref.getString(key, null);
        if (json == null) {
            return array;
        }

        ArrayList<Float> arrayList = new Gson().fromJson(json, new TypeToken<ArrayList<Float>>() {}.getType());
        int i = 0;
        for (Float f : arrayList) {
            array[i++] = f;
        }
        return array;
    }

    private void saveArray(SharedPreferences pref, String key, float[] array) {
        SharedPreferences.Editor editor = pref.edit();

        ArrayList<Float> out = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            out.add(array[i]);
        }

        String json = new Gson().toJson(out);
        editor.putString(key, json);
        editor.apply();
    }

    private void saveVolume() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_VOLUME, getVolume());
        editor.apply();
    }

    public void stop() {
        saveVolume();
        saveArray(sharedPreferences, KEY_SN_VOLUME, sQVolume);
    }

    public void newRotation() {
        if (counter > skipSamples) rotationDetected = true;
    }

}