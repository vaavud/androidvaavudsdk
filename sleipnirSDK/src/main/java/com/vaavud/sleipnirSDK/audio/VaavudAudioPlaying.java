package com.vaavud.sleipnirSDK.audio;

import android.media.AudioTrack;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;


public class VaavudAudioPlaying extends Thread {

		private static final String TAG = "SDK:AudioPlaying";

		private AudioTrack mPlayer;
		private double offset;
		private boolean isPlaying;

		private final int duration = 1; // seconds
		private final int sampleRate = 44100; //Hz
		private final int numSamples = duration * sampleRate;
		private short sample[] = new short[numSamples * 2];
		private final double freqOfTone = 14700; // hz

		private boolean mCalibrationMode = false;
		private String mFileName;
		private FileOutputStream os;

		public VaavudAudioPlaying(AudioTrack player, String fileName, boolean calibrationMode, Float playerVolume) {
				mPlayer = player;
				mFileName = fileName;
				mCalibrationMode = calibrationMode;

				offset = Math.PI;

				for (int i = 0; i < numSamples * 2; i = i + 2) {
						sample[i] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate / freqOfTone))) * Short.MAX_VALUE));
						sample[i + 1] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate / freqOfTone)) + offset) * Short.MAX_VALUE));
				}

				if (mCalibrationMode) {

						String filePath = fileName;
//		    Log.d(TAG, "FilePath: "+filePath);
						try {
								os = new FileOutputStream(filePath + ".play");
								os.write(short2byte(sample));
								os.close();
						} catch (IOException e) {
								e.printStackTrace();
						}
						os = null;
				}

				if (mPlayer != null && mPlayer.getState() != AudioTrack.STATE_UNINITIALIZED && mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
						mPlayer.stop();
				}

				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
//						Log.d(TAG,"Set Stereo Volume: "+playerVolume);
						mPlayer.setStereoVolume(playerVolume, playerVolume);
				} else {
//						Log.d(TAG,"SetVolume: "+playerVolume);
						mPlayer.setVolume(playerVolume);
				}
		}

		@Override
		public void run() {
				isPlaying = true;
				if (mPlayer.getState() == AudioTrack.STATE_INITIALIZED) {
						mPlayer.play();
						while (isPlaying && mPlayer!=null) {
								if (sample != null) {
										mPlayer.write(sample, 0, sample.length);
								}
						}
//	        Log.d(TAG, "Stop");
				}else{
//						Log.d(TAG, "Player not Initialized " + mPlayer.getState());
				}

		}

		/**
		 * Called from outside of the thread in order to stop the playback loop
		 */
		public void close() {
				if (mPlayer != null && mPlayer.getState() == AudioTrack.PLAYSTATE_PLAYING) {
						mPlayer.flush();
						mPlayer.stop();
						mPlayer.release();
				}
				isPlaying = false;
				sample = null;
		}

		//convert short to byte
		private byte[] short2byte(short[] sData) {
				int shortArrsize = sData.length;
				byte[] bytes = new byte[shortArrsize * 2];
				for (int i = 0; i < shortArrsize; i++) {
						bytes[i * 2] = (byte) (sData[i] & 0x00FF);
						bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
//		        sData[i] = 0;
				}
				return bytes;

		}


}
