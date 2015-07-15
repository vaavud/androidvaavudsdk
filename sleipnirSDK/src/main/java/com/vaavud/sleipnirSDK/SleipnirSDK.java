package com.vaavud.sleipnirSDK;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Pair;
import android.widget.Toast;

import com.vaavud.sleipnirSDK.algorithm.VaavudAudioProcessing;
import com.vaavud.sleipnirSDK.algorithm.VaavudWindProcessing;
import com.vaavud.sleipnirSDK.audio.VaavudAudioPlaying;
import com.vaavud.sleipnirSDK.audio.VaavudAudioRecording;
import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;

import java.util.List;


public class SleipnirSDK implements SignalListener{

		private static final String KEY_CALIBRATION_COEFFICENTS = "calibrationCoefficients";
		private static final String KEY_PLAYER_VOLUME = "playerVolume";

		private Context mContext;
		private Context appContext;

		private OrientationSensorManagerSleipnir orientationSensorManager;
		private boolean isMeasuring = false;

		private boolean mCalibrationMode;
		private Handler handler;

		private AudioManager myAudioManager;
		private AudioRecord recorder;
		private AudioTrack player;

		private final int duration = 1; // seconds
		private final int sampleRate = 44100; //Hz
		private final int numSamples = duration * sampleRate;

		private final int N = 3; //Hz
		private long initialTime;
		private Float[] coefficients;
		private Float playerVolume;

		private float calibrationProgress = 0F;


		private VaavudAudioPlaying audioPlayer;
		private VaavudAudioRecording audioRecording;
		private VaavudAudioProcessing vap;
		private VaavudWindProcessing vwp;
		private String mFileName;
		private SpeedListener speedListener;
		private SignalListener signalListener;

		private SettingsContentObserver mSettingsContentObserver;


		public SleipnirSDK(Context context, boolean calibrationMode) {
//		Log.d("SleipnirCoreController","Sleipnir Core Controller");
				mContext = context;
				mCalibrationMode = calibrationMode;
				appContext = mContext.getApplicationContext();

		}

		public void startController() {
				String coefficientsString;
				orientationSensorManager = new OrientationSensorManagerSleipnir(mContext);

				if (player != null) {
						player.flush();
						player.release();
				}
				if (recorder != null) {
						recorder.release();
				}

				player = null;
				recorder = null;
				initialTime = 0;
		}

		public void startMeasuring() {

				mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
				appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

				if (player == null)
						player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STREAM);
				if (recorder == null)
						recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, sampleRate * N);

				myAudioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
				int result = myAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);

				if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						Toast.makeText(appContext, R.string.connection_toast, Toast.LENGTH_LONG);
						// Start playback.
				}

				myAudioManager.setMicrophoneMute(false);

		}

		public void stopMeasuring() {
//		Log.d("SleipnirCoreController","Stop Measuring");
				pauseMeasuring();
				isMeasuring = false;
				appContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
		}


		public void pauseMeasuring() {
				if (isMeasuring) {
//			Log.d("SleipnirCoreController","Pause Measuring");
						if (audioPlayer != null) audioPlayer.close();
						if (audioRecording != null) audioRecording.close();

						if (orientationSensorManager != null && orientationSensorManager.isSensorAvailable()) {
								orientationSensorManager.stop();
						}

						//Object and thread destroying
						audioPlayer = null;
						audioRecording = null;
				}
		}

		public void resumeMeasuring() {

				if (isMeasuring) {

						int volume = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						final int maxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

						myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

						AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
						builder1.setTitle(appContext.getResources().getString(R.string.sound_disclaimer_title));
						builder1.setMessage(appContext.getResources().getString(R.string.sound_disclaimer));
						builder1.setCancelable(false);
						builder1.setNeutralButton(appContext.getResources().getString(R.string.button_ok),
										new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int id) {
														dialog.dismiss();
														myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
												}
										});

						AlertDialog alert = builder1.create();

						if (volume < maxVolume) {
//								Log.d("SleipnirCoreController", "Volume: " + volume + " " + maxVolume);
								alert.show();
						}
						if (orientationSensorManager.isSensorAvailable()) {
								orientationSensorManager.start();
						}

						audioPlayer = new VaavudAudioPlaying(player, mFileName, mCalibrationMode, playerVolume);

						audioRecording = new VaavudAudioRecording(recorder, player, speedListener, signalListener, mFileName, mCalibrationMode, playerVolume);
						audioRecording.setCoefficients(coefficients);
						audioPlayer.start();
						audioRecording.start();
						int bufferSizeRecording = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
						vap = new VaavudAudioProcessing(bufferSizeRecording / 10, speedListener, this, mFileName, mCalibrationMode, player, playerVolume);
						vwp = new VaavudWindProcessing(speedListener, signalListener, mCalibrationMode);

				}
		}

		public void stopController() {

				if (orientationSensorManager != null) {
						orientationSensorManager.stop();
						orientationSensorManager = null;
				}

				handler = null;
				if (player != null) player.release();
				if (recorder != null) recorder.release();
				player = null;
				recorder = null;
		}

		@Override
		public void signalChanged(short[] signal) {
				Pair<List<Integer>,Long> samplesResult = vap.processSamples(signal);
				for (int i=0;i<samplesResult.first.size();i++) {
						vwp.newTick(samplesResult.first.get(i));
				}
		}

		@Override
		public void signalChanged(float[] signal, float[] signalEstimated) {

		}

		@Override
		public void signalChanged(int mvgAvg, int diffFilter, float volumeLevel) {

		}
}
