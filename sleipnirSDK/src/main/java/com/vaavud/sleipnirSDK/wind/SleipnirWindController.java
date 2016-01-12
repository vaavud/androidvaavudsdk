package com.vaavud.sleipnirSDK.wind;


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

import com.vaavud.sleipnirSDK.OrientationSensorManagerSleipnir;
import com.vaavud.sleipnirSDK.R;
import com.vaavud.sleipnirSDK.SettingsContentObserver;
import com.vaavud.sleipnirSDK.audio.VaavudAudioPlaying;
import com.vaavud.sleipnirSDK.audio.VaavudAudioRecording;
import com.vaavud.sleipnirSDK.audio.VaavudVolumeAdjust;
import com.vaavud.sleipnirSDK.listener.AudioListener;
import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;

class Tick {
    public final long time;
    public final int deltaTime;

    public Tick(long time, int deltaTime) {
        this.time = time;
        this.deltaTime = deltaTime;
    }
}

class Rotation {
    public final long time;
    public final int timeOneRotation;
    public final float relRotationTime;
    public final Float heading;
    public final float[] relVelocities;

    public Rotation(long time, int timeOneRotation, float relRotationTime, Float heading, float[] relVelocities) {
        this.time = time;
        this.timeOneRotation = timeOneRotation;
        this.relRotationTime = relRotationTime;
        this.heading = heading;
        this.relVelocities = relVelocities;
    }
}


class Direction {
    public final long time;
    public final float globalDirection;
    public final float heading;

    public Direction(long time, float globalDirection, float heading) {
        this.time = time;
        this.globalDirection = globalDirection;
        this.heading = heading;
    }
}

interface TickReceiver {
    void newTick(Tick tick);
}

interface RotationReceiver {
    void newRotation(Rotation rotation);
}

interface DirectionReceiver {
    void newDirection(Direction direction);
}


public class SleipnirWindController implements AudioListener, RotationReceiver, DirectionReceiver {

    private static final String TAG = "SDK:Controller";
    private static final String KEY_CALIBRATION_COEFFICENTS = "calibrationCoefficients";
    private static final String KEY_PLAYER_VOLUME = "playerVolume";

    private Context mContext;
    private Context appContext;

    private OrientationSensorManagerSleipnir orientationSensorManager;
    private boolean isMeasuring = false;

    private AudioManager myAudioManager;
    private AudioRecord recorder;
    private AudioTrack player;

    private final int duration = 1; // seconds
    private final int sampleRate = 44100; //Hz
    private final int numSamples = duration * sampleRate;
    private int bufferSizeRecording = 512;
    private int minBufferSizeRecording;

    private final int N = 3; //Hz

    private long startTime;
    private long sampleCounter;


    private Float[] coefficients;
    private Float playerVolume = 1.0f;


    private VaavudAudioPlaying audioPlayer;
    private VaavudAudioRecording audioRecording;
    private AudioProcessor audioProcessor;
    private TickProcessor tickProcessor;
    private RotationProcessor rotationProcessor;
    private VaavudVolumeAdjust vaavudVolumeAdjust;
    private String mFileName;
    public SpeedListener speedListener;
    public SignalListener signalListener;

    private SettingsContentObserver mSettingsContentObserver;
    private int numRotations = 0;

    private SharedPreferences preferences;


    public SleipnirWindController(Context context) {
        Log.d(TAG, "Sleipnir Core Controller");
        mContext = context;
        appContext = mContext.getApplicationContext();
        preferences = appContext.getSharedPreferences("SleipnirSDKPreferences", Context.MODE_PRIVATE);
        mFileName = "filename";
        startController();
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
    }

    public void startMeasuring() {
        playerVolume = preferences.getFloat("playerVolume", 0.5f);

        myAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

        if (player == null)
            player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STREAM);

        if (recorder == null) {
            minBufferSizeRecording = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (minBufferSizeRecording < N * sampleRate) {
                minBufferSizeRecording = N * sampleRate;
            }
            Log.d(TAG, "MinBufferSizeRecording: " + minBufferSizeRecording);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSizeRecording);
        }

        myAudioManager.setMicrophoneMute(false);
        isMeasuring = true;

        startTime = System.nanoTime()/ 1000;
        sampleCounter = 0;

        resumeMeasuring();
    }


    private void resumeMeasuring() {

        if (isMeasuring) {

            final int maxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

            int volume = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

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
                alert.show();
            }
            if (orientationSensorManager != null) {
                orientationSensorManager = new OrientationSensorManagerSleipnir(mContext);
            }
            if (orientationSensorManager.isSensorAvailable()) {
                orientationSensorManager.start();
            }

            audioPlayer = new VaavudAudioPlaying(player, mFileName, playerVolume);
            audioRecording = new VaavudAudioRecording(recorder, this, bufferSizeRecording);

            vaavudVolumeAdjust = new VaavudVolumeAdjust(bufferSizeRecording, playerVolume);
            rotationProcessor = new RotationProcessor(this);
            tickProcessor = new TickProcessor(this);
            audioProcessor = new AudioProcessor(tickProcessor, bufferSizeRecording);


            audioPlayer.start();
            audioRecording.start();

        }
    }

    public void stopMeasuring() {
        pauseMeasuring();
        isMeasuring = false;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("playerVolume", playerVolume);
        editor.apply();
        appContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }


    private void pauseMeasuring() {
        if (isMeasuring) {
            if (audioPlayer != null) audioPlayer.close();
            if (audioRecording != null) audioRecording.close();

            audioProcessor.close();
            tickProcessor.close();

            if (orientationSensorManager != null && orientationSensorManager.isSensorAvailable()) {
                orientationSensorManager.stop();
            }
            myAudioManager.abandonAudioFocus(null);
            //Object and thread destroying
        }
    }

    public void stopController() {

        if (orientationSensorManager != null) {
            orientationSensorManager.stop();
            orientationSensorManager = null;
        }

        if (player != null) player.release();
        if (recorder != null) recorder.release();
        player = null;
        recorder = null;
    }

    public boolean isMeasuring() {
        return isMeasuring;
    }

    public double getOrientationAngle() {
        return orientationSensorManager.getAngle();
    }

    @Override
    public void newAudioBuffer(final short[] audioBuffer) {

        if (signalListener != null) signalListener.signalChanged(audioBuffer);

        if (audioBuffer == null) return;

        Pair<Integer, Double> noise = vaavudVolumeAdjust.noiseEstimator(audioBuffer);
        audioProcessor.processSamples(sampleCounter, audioBuffer);

        if (noise != null) {
            playerVolume = vaavudVolumeAdjust.newVolume(noise.first, noise.second, numRotations); // !!! numRotations

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                player.setStereoVolume(playerVolume, playerVolume);
            } else {
                player.setVolume(playerVolume);
            }

            numRotations = 0;
            tickProcessor.resetDetectionErrors();
        }

        sampleCounter += audioBuffer.length;
    }

    @Override
    public void newRotation(Rotation rotation) {

        // send to upwards


    }

    @Override
    public void newDirection(Direction direction) {
//        Log.d("TAG", direction.globalDirection);
    }
}
