package com.vaavud.sleipnirSDK.algorithm;

import android.media.AudioTrack;
import android.util.Pair;

import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VaavudAudioProcessing {


		private int windowAveragingSize;
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
		private boolean mVolumeCalibrated;

		//Buffer
		private short[] buffer;

		private FileOutputStream os = null;
		private VaavudWindProcessing vwp = null;
		private String mFileName;

		//Sound calibration
		private AudioTrack mPlayer;
		private final static int CALIBRATE_AUDIO_EVERY_X_BUFFER = 10;
		private int volumeAdjustCounter = 0;
		private float calibrationVolumeStep = 0.0f;
		private float currentVolume = 1.0f;
		private float maxVolume;
		private SpeedListener mSpeedListener = null;
		private SignalListener mSignalListener = null;


		public VaavudAudioProcessing() {
				buffer = null;
		}

		public VaavudAudioProcessing(int bufferSizeRecording, SpeedListener speedListener, SignalListener signalListener, String fileName, boolean calibrationMode, AudioTrack player, Float playerVolume) {
//				Log.d("SleipnirSDK", "VaavudAudioProcessing");
				mCalibrationMode = calibrationMode;
				mPlayer = player;

				mSignalListener = signalListener;
				mSpeedListener = speedListener;

				if (playerVolume != null && !calibrationMode) {
						mVolumeCalibrated = true;
						currentVolume = playerVolume;
				}
				maxVolume = AudioTrack.getMaxVolume();
				calibrationVolumeStep = maxVolume / 100;

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

				if (mCalibrationMode) {

						String filePath = fileName;
//		    Log.d("VaavudAudioProcessing", "FilePath: "+filePath);
						try {
								os = new FileOutputStream(filePath + ".rec");
						} catch (FileNotFoundException e) {
								e.printStackTrace();
						}
				}

				vwp = new VaavudWindProcessing(speedListener, signalListener, mCalibrationMode);
		}

		public void writeToDataFile() {
				try {
						os.write(short2byte(buffer));
				} catch (IOException e) {
						e.printStackTrace();
				}

		}


		public Pair<List<Integer>, Long> processSamples(short[] inputBuffer) {

				if (inputBuffer != null) {

						System.arraycopy(inputBuffer, 0, buffer, 0, inputBuffer.length);
						List<Integer> samplesDistanceTick = new ArrayList<Integer>();

						if (mCalibrationMode) {
								writeToDataFile();
						}

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
//	        			Log.d("VaavudAudioProcessing", "Current Sample: " + currentSample);

								// Moving Diff Update buffer value
								mvgDiff[bufferIndex] = Math.abs(currentSample - mvgAvg[bufferIndexLast]); // ! need to use old mvgAvgValue so place before mvgAvg update
								// Moving avg Update buffer value
								mvgAvg[bufferIndex] = currentSample;

								// Moving Avg update SUM
								mvgAvgSum += mvgAvg[bufferIndex];
								mvgDiffSum += mvgDiff[bufferIndex];

								if (maxDiff < mvgDiffSum) {
										maxDiff = mvgDiffSum;
								}

								if (mSignalListener != null) {
//						Log.d("AudioProcessing","Sending AVG Signal");
										mSignalListener.signalChanged(mvgAvgSum, mvgDiffSum, currentVolume);
								}

								if (detectTick((int) (counter - lastTick))) {
										//Direction Detection Algorithm
//	        	Log.d("AudioProcessing","sampleSinceTick: "+ counter + " : " + lastTick);

										lastMvgMax = mvgMax;
										lastMvgMin = mvgMin;
										lastDiffMax = diffMax;
										lastDiffMin = diffMin;
										lastMvgGapMax = mvgGapMax;
//	            Log.d("AudioProcessing",lastMvgMax+":"+lastMvgMin+":"+lastDiffMax+":"+lastDiffMin+":"+lastMvgGapMax);

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
//				if (!mVolumeCalibrated) {
//						if (diffMax > 3.5 * Short.MAX_VALUE && volumeAdjustCounter > CALIBRATE_AUDIO_EVERY_X_BUFFER) {
//								//						Log.d("SleipnirSDK", "diffMax: " + diffMax);
//								currentVolume -= calibrationVolumeStep;
//								adjustVolume();
//								volumeAdjustCounter = 0;
//						}
//						//				Log.d("SleipnirSDK", "mvgMin: " + mvgMin);
//						if ((mvgMin < -2*Short.MAX_VALUE && diffMax > 1 * Short.MAX_VALUE) && volumeAdjustCounter > CALIBRATE_AUDIO_EVERY_X_BUFFER) {
//								if (mvgMin < -2.5*Short.MAX_VALUE) {
//										this.currentVolume -= 10 * calibrationVolumeStep;
//								} else {
//										currentVolume -= calibrationVolumeStep;
//								}
//								//						Log.d("SleipnirSDK", "mvgMin: " + mvgMin + " diffMax: " + diffMax);
//								adjustVolume();
//								volumeAdjustCounter = 0;
//						}
//
//						if (volumeAdjustCounter > 20 * CALIBRATE_AUDIO_EVERY_X_BUFFER) {
//								Log.d("SleipnirSDK", "Sound Calibrated: " + currentVolume);
//								mVolumeCalibrated = true;
//								mSpeedListener.volumeLevel(currentVolume);
//						}
//
//						if (mCalibrationMode) {
//								Log.d("SleipnirSDK", "SOUND:Adjusting volume while calibrating: " + currentVolume);
//								mSpeedListener.volumeLevel(currentVolume);
//						}
//
//						volumeAdjustCounter++;
//				}
						return Pair.create(samplesDistanceTick, (counter - lastTick));
				}
				return null;
		}

		private void adjustVolume() {

				if (currentVolume > 1.0f) currentVolume = 1.0f;
				if (currentVolume < 0.1) currentVolume = 1.0f;
//	    Log.d("SleipnirSDK","Adjusting Volume: "+ currentVolume);
				if (mSignalListener != null) {
						mSpeedListener.volumeLevel(currentVolume);
				}
		}


		private boolean detectTick(int sampleSinceTick) {
//		Log.d("AudioProcessing","MvgState: "+mvgState + " diffState: "+ diffState);
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

//	            if (mvgDiffSum > 0.3*lastDiffMax && mvgAvgSum < 0.2*lastMvgMin) { // diff was 1200
								if (((mvgAvgSum < mvgGapMax - mvgDropHalf) && (mvgDiffSum > diffRiseThreshold)) || (mvgDiffSum > 0.5 * lastDiffMax)) {
										return true;
								}
								break;
						default:
								break;
				}
//	    Log.d("AudioProcessing","mvgAvgSum: "+mvgAvgSum+" mvgMax: "+mvgMax);
				if (mvgAvgSum > mvgMax)
						mvgMax = mvgAvgSum;

				if (mvgDiffSum > diffMax)
						diffMax = mvgDiffSum;

				if (mvgDiffSum < diffMin)
						diffMin = mvgDiffSum;

				if (sampleSinceTick == 6000) {
						lastTick = counter;
//	    	Log.d("AudioProcessing", "Reset State machine: "+sampleSinceTick);
						resetStateMachine();
				}

				return false;


		}

		public int getWindowAveragingSize() {
				return windowAveragingSize;
		}

		public void setWindowAveragingSize(int windowAveragingSize) {
				this.windowAveragingSize = windowAveragingSize;
		}


//		public long processSamples(short[] inputBuffer) {
//				if (inputBuffer != null) {
//						System.arraycopy(inputBuffer, 0, buffer, 0, inputBuffer.length);
//
//						if (mCalibrationMode) {
//								writeToDataFile();
//						}
//						return applyFilter();
//				} else {
//						return -1;
//				}
//		}

		private void resetStateMachine() {
//		Log.d("AudioProcessing", "ResetStateMachine");

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

		public void setPlayer(AudioTrack player) {
				mPlayer = player;
		}


		public void close() {
				buffer = null;
				mvgAvg = null;
				mvgDiff = null;
				vwp.close();
				vwp = null;
				if (mCalibrationMode && os != null) {
						try {
								os.close();
						} catch (IOException e) {
								e.printStackTrace();
						}
				}
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

		public void setCoefficients(Float[] coefficients) {
				if (vwp != null) {
						vwp.setCoefficients(coefficients);
				}

		}

}
