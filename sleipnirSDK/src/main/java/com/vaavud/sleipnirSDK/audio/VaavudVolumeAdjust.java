package com.vaavud.sleipnirSDK.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
//import android.util.Log;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by juan on 16/07/15.
 */
public class VaavudVolumeAdjust {

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

		private int sampleRate = 44100;
		private int skipSamples = 0;
		private int nSamples = 100;
		private long counter;
		private List<Integer> diffValues;
		private double sN;
		private int samplesPerBuffer;
		private int volumeLevel = VOLUME_STEPS / 2;

		public VaavudVolumeAdjust(int audioBufferSize, Float playerVolume) {
				diffValues = new ArrayList<Integer>();
				counter = 0;
				sN = 0;
				samplesPerBuffer = 100;
				volumeLevel = (int) (playerVolume * 100);
				if (volumeLevel != 50) {
						volState = STEEPESTASCENT_STATE;
				}
				buffer = new short[audioBufferSize];
//				Log.d(TAG,"Buffer Size: "+AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
				skipSamples = (int) (AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2);
				samplesPerBuffer = (int) (0.01f * audioBufferSize);
		}

		public Pair<Integer, Double> noiseEstimator(short[] audioBuffer) {
//				Log.d(TAG, "Samples per buffer :" + samplesPerBuffer+ "Volume Level: "+volumeLevel);
				counter += audioBuffer.length;
				if (counter > skipSamples) {
						System.arraycopy(audioBuffer, 0, buffer, 0, buffer.length);
						for (int i = 0; (i < samplesPerBuffer) && (diffValues.size() < nSamples); i++) {
								int index = ((int) Math.random() * (buffer.length - 2)) + 1;
								int diff = 0;
								for (int j = 0; j < 3; j++) {
										diff = diff + Math.abs(buffer[index + j] - buffer[index + j + 1]);
								}
								diffValues.add(diff);
						}
						if (diffValues.size() == nSamples) {
								Collections.sort(diffValues);
								int diff20 = diffValues.get(19);
								sN = (double) diffValues.get(79) / (double) diffValues.get(39);
								counter = 0;
								diffValues.clear();
//								Log.d(TAG, "NOISE: Diff20: " + diff20 + " SNR: " + sN);
								return Pair.create(diff20, sN);
						}
				}

				return null;
		}

		public float newVolume(int diff20, double sN, int numRotations, int detectionErrors) {
//
//				if (diff20<NOISE_THRESHOLD){
//						volumeLevel +=10;
//										Log.d(TAG,"VolumeLevel: "+ volumeLevel);
//						return volumeLevel/100;
//				}
				volumeCounter++;
				float volumeChange = 0.0f;
				if (sN > 6 && numRotations >= 1) {
						volState = STEEPESTASCENT_STATE;
				}
//				Log.d(TAG, "INPUT VOL: " + "VolState: " + volState + " Diff20: " + diff20 + " sN: " + sN + " NumRotations: " + numRotations + " detectionErrors: " + detectionErrors + " VolumeCounter: " + volumeCounter);
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
//								Log.d(TAG, "VolState: " + volState + " VolLevel: " + volumeLevel + " VolChange: " + volumeChange + " VolumeCounter" + volumeCounter);
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
//								Log.d(TAG, "VolState: " + volState + " VolLevel: " + volumeLevel);
								break;

						case STEEPESTASCENT_STATE:
//								Log.d(TAG, "STEEPESTASCENT_STATE sN: "+sN + " numRotations: "+numRotations);
								boolean signalIsGood = (sN > 1.2 && numRotations >= 1);
								if (signalIsGood) {
//										Log.d(TAG, "SignalIsGood volumeLevel: " + volumeLevel + "snVolume: " + sNVolume[volumeLevel]);
										sNVolume[volumeLevel] = ((sNVolume[volumeLevel] == 0.0d) ? sN : sNVolume[volumeLevel] * 0.7 + 0.3 * sN);
										volumeCounter = 0;
								} else {
//										Log.d(TAG, "SignalIsNotGood sN: "+sN + " numRotations: "+numRotations);
										if (volumeCounter > 40) {
												returnToDiffState();
												break;
										}
								}
								switch (expState) {
										case TOP_STATE:
//												Log.d(TAG, "TOP_STATE");
												int bestSNVol = bestSNVolume();
												if (sNVolume[bestSNVol] < 6) {
														returnToDiffState();
														break;
												}

												volumeChange = bestSNVol - volumeLevel;
												volumeChange = (volumeChange >= 1 && volumeChange < 5) ? 1 : (volumeChange <= -1 && volumeChange > -5) ? -1 : volumeChange;
												volumeLevel = (int) (volumeLevel + volumeChange);
//												Log.d(TAG, "ExploreState: " + expState + " VolLevel: " + volumeLevel + " VolChange: " + volumeChange);
												if (volumeChange == 0) {
														expState = EXPLORE_STATE;
												}
												break;

										case EXPLORE_STATE:
//												Log.d(TAG, "EXPLORE_STATE");
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

				if (volumeLevel < 5) {
						volumeLevel = 5;
				} else if (volumeLevel > VOLUME_STEPS-1) {
						volumeLevel = VOLUME_STEPS-1;
				}
//				Log.d(TAG, "OUTPUT VOL: Diff20: " + diff20 + " sN: " + sN + " NumRotations: " + numRotations + " detectionErrors: " + detectionErrors + " VolumeLevel " + volumeLevel);
				return getVolume();
		}

		private float getVolume() {
				return (float) volumeLevel / (float) VOLUME_STEPS;
		}

		private void returnToDiffState() {
//				Log.d(TAG, "Return to Diff State");
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
}