package com.vaavud.sleipnirSDK;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.vaavud.sleipnirSDK.algorithm.VaavudAudioProcessing;
import com.vaavud.sleipnirSDK.algorithm.VaavudWindProcessing;
import com.vaavud.sleipnirSDK.audio.VaavudAudioPlaying;
import com.vaavud.sleipnirSDK.audio.VaavudAudioRecording;
import com.vaavud.sleipnirSDK.audio.VaavudVolumeAdjust;
import com.vaavud.sleipnirSDK.listener.AudioListener;
import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;

import java.util.List;


public class SleipnirSDKController implements AudioListener {

		private static final String TAG = "SDK:Controller";
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
		private int bufferSizeRecording = 512;
		private int minBufferSizeRecording;

		private final int N = 3; //Hz
		private long initialTime;
		private Float[] coefficients;
		private Float playerVolume=1.0f;

		private float calibrationProgress = 0F;


		private VaavudAudioPlaying audioPlayer;
		private VaavudAudioRecording audioRecording;
		private VaavudAudioProcessing vap;
		private VaavudWindProcessing vwp;
		private VaavudVolumeAdjust vva;
		private String mFileName;
		private SpeedListener speedListener;
		private SignalListener signalListener;

		private SettingsContentObserver mSettingsContentObserver;
		private int numRotations = 0;

		private SharedPreferences preferences;

//		private float previousVolume = 1.0f;
//		private int[] diff20List;
//		private float[] sNList;


		public SleipnirSDKController(Context context, boolean calibrationMode, SpeedListener speedListener, SignalListener signalListener, Float[] coefficients, String fileName) {
//		Log.d("SleipnirCoreController","Sleipnir Core Controller");
				mContext = context;
				mCalibrationMode = calibrationMode;
				appContext = mContext.getApplicationContext();
				preferences = appContext.getSharedPreferences("SleipnirSDKPreferences", Context.MODE_PRIVATE);
				this.speedListener = speedListener;
				this.signalListener = signalListener;
				this.coefficients = coefficients;
				mFileName = fileName;
//				Log.d(TAG, "MFileName Constructor: "+mFileName);
//				diff20List = new int[101];
//				sNList = new float[101];

		}

		public void startController() {

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
				Log.d(TAG, "Start Measuring");
				playerVolume= preferences.getFloat("playerVolume",1.0f);

				Log.d(TAG,"Player Volume: "+playerVolume);
				mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
				appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

				if (player == null)
						player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STREAM);

				if (recorder == null) {
						minBufferSizeRecording = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
						if (minBufferSizeRecording < N * sampleRate) {
								minBufferSizeRecording = N * sampleRate;
						}
//						Log.d(TAG, "MinBufferSizeRecording: " + minBufferSizeRecording);
						recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSizeRecording);
				}


//				Log.d(TAG, "Player Status: " + player.getState());
//				Log.d(TAG, "Recorder Status: " + recorder.getState());

				myAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
				int result = myAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
//				Log.d(TAG, "MyAudioManager result: " + result);
				if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
						Toast.makeText(mContext, R.string.connection_toast, Toast.LENGTH_LONG).show();
						// Start playback.
				} else {
						Toast.makeText(mContext, R.string.permision_toast, Toast.LENGTH_LONG).show();
				}

				myAudioManager.setMicrophoneMute(false);
				isMeasuring = true;
				resumeMeasuring();
		}

		public void stopMeasuring() {
//				Log.d("SleipnirCoreController", "Stop Measuring");
				pauseMeasuring();
				isMeasuring = false;
				SharedPreferences.Editor editor = preferences.edit();
				editor.putFloat("playerVolume", playerVolume);
				editor.commit();
				appContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
		}


		public void pauseMeasuring() {
				if (isMeasuring) {

//						Log.d(TAG, "Pause Measuring");
						if (audioPlayer != null) audioPlayer.close();
						if (audioRecording != null) audioRecording.close();

						vap.close();
						vwp.close();

						if (orientationSensorManager != null && orientationSensorManager.isSensorAvailable()) {
								orientationSensorManager.stop();
						}
						myAudioManager.abandonAudioFocus(null);
						//Object and thread destroying

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

//						Log.d(TAG,"Player Volume: "+playerVolume);
//						Log.d(TAG,"Filename: "+ mFileName);
						audioPlayer = new VaavudAudioPlaying(player, mFileName, mCalibrationMode, playerVolume);
						audioRecording = new VaavudAudioRecording(recorder, this, bufferSizeRecording);

						vva = new VaavudVolumeAdjust(bufferSizeRecording,playerVolume);
						vap = new VaavudAudioProcessing(bufferSizeRecording, mFileName, mCalibrationMode);
						vwp = new VaavudWindProcessing(speedListener, signalListener, mCalibrationMode);

						if (coefficients!=null) {
								vwp.setCoefficients(coefficients);
						}

						audioPlayer.start();
						audioRecording.start();


				}
		}

		public boolean isMeasuring(){
				return  isMeasuring;
		}

		public double getOrientationAngle() {
				return orientationSensorManager.getAngle();
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
		public void newAudioBuffer(final short[] audioBuffer) {

				if (signalListener != null) signalListener.signalChanged(audioBuffer);

				if (audioBuffer != null) {
//						Log.d("SleipnirSDKController","New Audio Buffer");
						Pair<Integer, Double> noise = vva.noiseEstimator(audioBuffer);
						Pair<List<Integer>, Long> samplesResult = vap.processSamples(audioBuffer);
						for (int i = 0; i < samplesResult.first.size(); i++) {
//								Log.d(TAG,"Tick Value: "+samplesResult.first.get(i));
								if (vwp.newTick(samplesResult.first.get(i))) numRotations++;
						}
						if (noise != null) {
								int detectionErrors = vwp.getTickDetectionErrorCount();
								playerVolume = vva.newVolume(noise.first, noise.second, numRotations, detectionErrors);
								Log.d(TAG,"Volume: "+playerVolume);
//								playerVolume = 0.95f;
//												volume = previousVolume;

//												int index = (int)(volume*100);

//												diff20List[(int)(volume*100)]=noise.first;
//												sNList[(int)(volume*100)] = noise.second.floatValue();

								if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
										player.setStereoVolume(playerVolume, playerVolume);
								} else {
										player.setVolume(playerVolume);
								}

//												previousVolume=previousVolume-0.01f;
//												if (previousVolume < 0 ) previousVolume=0.0f;
								numRotations = 0;
								vwp.resetDetectionErrors();
						}
				}
		}

//		private Float[] asFloatObjectArray(String array) {
//						if (array == null) {
//								return null;
//						}
//						array = array.replace("[", "").replace("]", "");
//						String[] values = array.split(",");
//						Float[] result = new Float[values.length];
//						for (int i = 0; i < values.length; i++) {
//								String v = values[i];
//								result[i] = Float.parseFloat(v);
//						}
//						return result;
//		}

//		public int[] getDiff20() {
//				return diff20List;
//		}
//
//		public float[] getsN() {
//				return sNList;
//		}
}
