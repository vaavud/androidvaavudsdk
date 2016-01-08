package com.vaavud.sleipnirSDK.algorithm;


import android.util.Log;
import android.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VaavudAudioProcessing {

    private static final String TAG = "SDK:AudioProcessing";
    //Sound processing
    private int[] mvgAvg = new int[3];
    private int mvgAvgSum;
    private int[] mvgDiff = new int[3];
    private int mvgDiffSum;
    private int lastValue;
    private double gapBlock;
    private long counter;
    private long lastTick;
    private short mvgState;
    private short diffState;
    private int mvgMax;
    private int mvgMin;
    private int lastMvgMax;
    private int lastMvgMin;
    private int diffMax;
    private int diffMin;
    private int lastDiffMax;
    private int lastDiffMin;
    private int diffGap;
    private int mvgGapMax;
    private int lastMvgGapMax;
    private int mvgDropHalf;
    private int diffRiseThreshold;
    private boolean mCalibrationMode;

    //Buffer
    private short[] buffer;

    private VaavudWindProcessing vwp = null;


    public VaavudAudioProcessing() {
        buffer = null;
    }

    public VaavudAudioProcessing(int bufferSizeRecording, String fileName, boolean calibrationMode) {
        mCalibrationMode = calibrationMode;

        buffer = new short[bufferSizeRecording];

        //SoundProcessing Init
        counter = 0;
        mvgAvgSum = 0;
        mvgDiffSum = 0;
        lastValue = 0;

        lastDiffMax = Short.MAX_VALUE;
        lastDiffMin = 0;
        lastMvgMax = Short.MAX_VALUE / 2;
        lastMvgMin = -Short.MAX_VALUE / 2;
        lastMvgGapMax = 0;
        lastTick = 0;

        mvgMax = 0;
        mvgMin = 0;
        diffMax = 0;
        diffMin = 0;

        gapBlock = 0;
        mvgState = 0;
        diffState = 0;
    }


    public Pair<List<Integer>, Long> processSamples(short[] inputBuffer) {

        if (inputBuffer != null) {

            System.arraycopy(inputBuffer, 0, buffer, 0, inputBuffer.length);
            List<Integer> samplesDistanceTick = new ArrayList<Integer>();

            int maxDiff = 0;
            int currentSample = 0;

            for (int i = 0; i < buffer.length; i++) {
                int bufferIndex = (int) (mod(counter, 3));
                int bufferIndexLast = (int) (mod(counter - 1, 3));

                // Moving Avg subtract
                mvgAvgSum -= mvgAvg[bufferIndex];
                // Moving Diff subtrack
                mvgDiffSum -= mvgDiff[bufferIndex];

                currentSample = buffer[i];

                // Moving Diff Update buffer value
                mvgDiff[bufferIndex] = Math.abs(currentSample - mvgAvg[bufferIndexLast]); // ! need to use old mvgAvgValue so place before mvgAvg update
                // Moving avg Update buffer value
                mvgAvg[bufferIndex] = currentSample;

                // Moving Avg update SUM
                mvgAvgSum += mvgAvg[bufferIndex];
                mvgDiffSum += mvgDiff[bufferIndex];

                if (maxDiff < mvgDiffSum)
                    maxDiff = mvgDiffSum;

                if (detectTick((int) (counter - lastTick))) {
                    lastMvgMax = mvgMax;
                    lastMvgMin = mvgMin;
                    lastDiffMax = diffMax;
                    lastDiffMin = diffMin;
                    lastMvgGapMax = mvgGapMax;

                    mvgMax = 0;
                    mvgMin = 0;
                    diffMax = 0;
                    diffMin = 6 * Short.MAX_VALUE;
                    mvgState = 0;
                    diffState = 0;
                    samplesDistanceTick.add((int) (counter - lastTick));
                    lastTick = counter;
                }
                counter++;
            }

            return Pair.create(samplesDistanceTick, (counter - lastTick));
        }
        return null;
    }

    private boolean detectTick(int sampleSinceTick) {
        switch (mvgState) {
            case 0:
                if (sampleSinceTick < 60) {
                    if (mvgAvgSum > 0.5 * lastMvgMax) {
                        mvgState = 1;
                    }
                } else {
                    mvgState = -1;
                }
                break;
            case 1:
                if (sampleSinceTick < 90) {
                    if (mvgAvgSum < 0.5 * lastMvgMin) {
                        return true;
                    }
                } else {
                    mvgState = -1;
                }
                break;
            default:
                break;
        }

        switch (diffState) {
            case 0:
                if (mvgAvgSum < mvgMin)
                    mvgMin = mvgAvgSum;
                if (mvgDiffSum > 0.3 * lastDiffMax)
                    diffState = 1;
                break;
            case 1:
                if (mvgAvgSum < mvgMin)
                    mvgMin = mvgAvgSum;
                if (mvgAvgSum > 0)
                    diffState = 2;
                break;
            case 2:
                if (mvgDiffSum < 0.35 * lastDiffMax) {
                    diffState = 3;
                    gapBlock = sampleSinceTick * 2.5;
                    if (gapBlock > 5000) {
                        gapBlock = 5000;
                    }
                }
                break;
            case 3:
                if (sampleSinceTick > gapBlock) {
                    diffState = 4;
                    diffGap = mvgDiffSum;
                    mvgGapMax = mvgAvgSum;
                    diffRiseThreshold = (int) (diffGap + 0.1 * (lastDiffMax - diffGap));
                    mvgDropHalf = (lastMvgGapMax - mvgMin) / 2;
                }
                break;
            case 4:
                if (mvgAvgSum > mvgGapMax)
                    mvgGapMax = mvgAvgSum;

                if (((mvgAvgSum < mvgGapMax - mvgDropHalf) && (mvgDiffSum > diffRiseThreshold)) || (mvgDiffSum > 0.5 * lastDiffMax)) {
                    return true;
                }
                break;
            default:
                break;
        }
        if (mvgAvgSum > mvgMax)
            mvgMax = mvgAvgSum;

        if (mvgDiffSum > diffMax)
            diffMax = mvgDiffSum;

        if (mvgDiffSum < diffMin)
            diffMin = mvgDiffSum;

        if (sampleSinceTick == 6000) {
            lastTick = counter;
//	    	Log.d(TAG, "Reset State machine: "+sampleSinceTick);
            resetStateMachine();
        }

        return false;


    }

    private void resetStateMachine() {
//		Log.d(TAG, "ResetStateMachine");

        mvgState = 0;
        diffState = 0;
        gapBlock = 0;

        mvgMax = 0;
        mvgMin = 0;
        diffMax = 0;
        diffMin = 0;

        lastMvgMax = Short.MAX_VALUE / 2;
        lastMvgMin = -Short.MAX_VALUE / 2;
        lastDiffMax = Short.MAX_VALUE;
        lastDiffMin = 0;
        lastMvgGapMax = 0;
    }

    public void close() {
        buffer = null;
        mvgAvg = null;
        mvgDiff = null;
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private int mod(long l, int y) {
        int result = (int) (l % y);
        if (result < 0) {
            result += y;
        }
        return result;
    }
}
