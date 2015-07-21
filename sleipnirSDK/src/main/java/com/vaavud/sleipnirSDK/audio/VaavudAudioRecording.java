package com.vaavud.sleipnirSDK.audio;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import com.vaavud.sleipnirSDK.algorithm.VaavudAudioProcessing;
import com.vaavud.sleipnirSDK.algorithm.VaavudWindProcessing;
import com.vaavud.sleipnirSDK.listener.AudioListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;

public class VaavudAudioRecording extends Thread {
		private boolean stopped = false;
		private AudioListener mAudioListener = null;
		private int mBufferSizeRecording;
		private AudioRecord mRecorder = null;
		private AudioTrack mPlayer = null;
		private short[] buffer = null;
		private int bytesRead = 0;
		private VaavudAudioProcessing vap;
		private VaavudWindProcessing vwp;
		private VaavudVolumeAdjust vva;

		/**
		 * Give the thread high priority so that it's not canceled unexpectedly, and start it
		 */
		public VaavudAudioRecording(AudioRecord recorder, AudioTrack player, SpeedListener speedListener, AudioListener audioListener, int bufferSizeRecording, VaavudVolumeAdjust vva, VaavudAudioProcessing vap, VaavudWindProcessing vwp) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				mAudioListener = audioListener;
				mRecorder = recorder;
				mPlayer = player;
				mBufferSizeRecording = bufferSizeRecording;
				this.vva = vva;
				this.vap = vap;
				this.vwp = vwp;
				if (mRecorder != null && mRecorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
						if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
								Log.d("VaavudAudioRecorder", "mRecorderStoped");
								mRecorder.stop();
						}
				}
		}

		@Override
		public void run() {
				/*
				 * Initialize buffer to hold continuously recorded audio data, start recording, and start
         * playback.
         */
				int numRotations = 0;
				float previousVolume = 1.0f;
				try {
						stopped = false;
						mRecorder.startRecording();
						buffer = new short[mBufferSizeRecording];
//						Log.d("SleipnirCoreController", "Recording Status: " + mRecorder.getRecordingState());
						/*
						 * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */

						while (!stopped) {
								bytesRead = mRecorder.read(buffer, 0, buffer.length);
//								Log.d("VaavudAudioRecording", "Numer of bits read : " + bytesRead);
								if (mAudioListener != null) {
										mAudioListener.newAudioBuffer(buffer);
								}

//								Pair<Integer, Double> noise = vva.noiseEstimator(buffer);
//								Pair<List<Integer>, Long> samplesResult = vap.processSamples(buffer);
//								for (int i = 0; i < samplesResult.first.size(); i++) {
//										if (vwp.newTick(samplesResult.first.get(i))) numRotations++;
//								}
//								if (noise != null) {
////						if (Math.random() > 0.5) {
////								volume = 0.20f;
////						}
////						else {
////								volume = 0.33f;
////						}
//										Log.d("SleipnirSDKController", "NOISE: Diff20: " + noise.first + " SNR: " + noise.second + " Volume: " + previousVolume);
//										int detectionErrors = vwp.getTickDetectionErrorCount();
//										float volume = vva.newVolume(noise.first, noise.second, numRotations, detectionErrors);
//										Log.d("SleipnirNewVolume", "Volume: " + volume);
//										if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//												mPlayer.setStereoVolume(volume, volume);
//										} else {
//												mPlayer.setVolume(volume);
//										}
//										previousVolume = volume;
//										numRotations = 0;
//										vwp.resetDetectionErrors();
//								}
//              buffer=null;
						}
//						Log.d("AudioRecorder", "Stop");
				} catch (Throwable x) {
						Log.w("SleipnirCoreController", "Error reading voice audio" + x.getMessage());
//						stopped = true;
				}
				/*
         * Frees the thread's resources after the loop completes so that it can be run again */ finally {
//						Log.d("SleipnirCoreController", "Executing Finally");
						buffer = null;
//						vap.close();
//						vap = null;
				}
		}

//		public void setCoefficients(Float[] coefficients) {
//				vap.setCoefficients(coefficients);
//		}

		/**
		 * Called from outside of the thread in order to stop the recording/playback loop
		 */
		public void close() {
				stopped = true;
				buffer = null;
				if (mRecorder != null && mRecorder.getState() == AudioRecord.RECORDSTATE_RECORDING) {
						mRecorder.stop();
						mRecorder.release();
				}
		}
}
