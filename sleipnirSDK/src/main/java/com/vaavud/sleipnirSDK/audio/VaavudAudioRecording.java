package com.vaavud.sleipnirSDK.audio;

import android.media.AudioRecord;
import android.util.Log;

import com.vaavud.sleipnirSDK.listener.AudioListener;


public class VaavudAudioRecording extends Thread {
		private static final String TAG = "SDK:AudioRecording";

		private boolean stopped = false;
		private AudioListener mAudioListener = null;
		private int mBufferSizeRecording;
		private AudioRecord mRecorder = null;
		private short[] buffer = null;
		private int bytesRead = 0;


		/**
		 * Give the thread high priority so that it's not canceled unexpectedly, and start it
		 */
		public VaavudAudioRecording(AudioRecord recorder, AudioListener audioListener, int bufferSizeRecording) {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				mAudioListener = audioListener;
				mRecorder = recorder;
				mBufferSizeRecording = bufferSizeRecording;
				if (mRecorder != null && mRecorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
						if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
//								Log.d(TAG, "mRecorderStoped");
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

				stopped = false;
				mRecorder.startRecording();
				buffer = new short[mBufferSizeRecording];
//						Log.d("SleipnirCoreController", "Recording Status: " + mRecorder.getRecordingState());
						/*
						 * Loops until something outside of this thread stops it.
             * Reads the data from the recorder and writes it to the audio track for playback.
             */
//				Log.d(TAG, "Recording status: " + mRecorder.getRecordingState());
				while (!stopped) {
						try {
								bytesRead = mRecorder.read(buffer, 0, buffer.length);
								if (mAudioListener != null && bytesRead > 0) {
										mAudioListener.newAudioBuffer(buffer);
								}
//								Log.d(TAG, "Numer of bits read : " + bytesRead);
						} catch (Throwable x) {
//								Log.w(TAG, "Error reading voice audio" + x.getMessage());
								stopped = true;
						}
				}
//				/*
//         * Frees the thread's resources after the loop completes so that it can be run again */ finally {
////						Log.d("SleipnirCoreController", "Executing Finally");
//				buffer = null;

//				}
		}


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
