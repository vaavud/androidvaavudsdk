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

import com.vaavud.sleipnirSDK.OrientationSensorManagerSleipnir;
import com.vaavud.sleipnirSDK.R;
import com.vaavud.sleipnirSDK.SettingsContentObserver;
import com.vaavud.sleipnirSDK.audio.AudioPlayer;
import com.vaavud.sleipnirSDK.audio.AudioRecorder;
import com.vaavud.sleipnirSDK.audio.VolumeAdjust;
import com.vaavud.sleipnirSDK.listener.AudioListener;
import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;
import com.vaavud.sleipnirSDK.model.SpeedEvent;

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

    private static final String TAG = "SDK:SleipnirWC";

    private Context mContext;
    private Context appContext;

    private OrientationSensorManagerSleipnir orientationSensorManager;
    private boolean isMeasuring = false;

    private AudioManager myAudioManager;
    private AudioRecord recorder;
    private AudioTrack player;

    private final int sampleRate = 44100; //Hz
    private int processBufferSize = 512;
    private int audioBufferSize;

    private long startTime;
    private long sampleCounter;


    private Float[] coefficients;

    private AudioPlayer audioPlayer;
    private AudioRecorder audioRecorder;
    private AudioProcessor audioProcessor;
    private TickProcessor tickProcessor;
    private RotationProcessor rotationProcessor;
    private VolumeAdjust volumeAdjust;
    public SpeedListener speedListener;
    public SignalListener signalListener;

    private SettingsContentObserver mSettingsContentObserver;
    private SharedPreferences preferences;

    private Throttle throttleSpeed = new Throttle(200);

    public SleipnirWindController(Context context) {
        mContext = context;
        appContext = mContext.getApplicationContext();
        preferences = appContext.getSharedPreferences("SleipnirSDKPreferences", Context.MODE_PRIVATE);
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
        myAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);

        if (player == null)
            player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, sampleRate * 2, AudioTrack.MODE_STREAM);

        if (recorder == null) {
            audioBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (audioBufferSize < 3 * sampleRate) audioBufferSize = 3 * sampleRate;
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
        }

        myAudioManager.setMicrophoneMute(false);
        isMeasuring = true;

        startTime = System.currentTimeMillis();
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

            audioPlayer = new AudioPlayer(player);
            audioRecorder = new AudioRecorder(recorder, this, processBufferSize);

            volumeAdjust = new VolumeAdjust(processBufferSize, audioBufferSize, preferences.getFloat("playerVolume", 0.5f));
            setVolume(volumeAdjust.getVolume());

            rotationProcessor = new RotationProcessor(this);
            tickProcessor = new TickProcessor(this);
            audioProcessor = new AudioProcessor(tickProcessor, processBufferSize);

            audioPlayer.start();
            audioRecorder.start();
        }
    }

    public void stopMeasuring() {
        pauseMeasuring();
        isMeasuring = false;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("playerVolume", volumeAdjust.getVolume());
        editor.apply();
        appContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }


    private void pauseMeasuring() {
        if (isMeasuring) {
            if (audioPlayer != null) audioPlayer.close();
            if (audioRecorder != null) audioRecorder.close();

            audioProcessor.close();
            tickProcessor.close();

            if (orientationSensorManager != null && orientationSensorManager.isSensorAvailable()) {
                orientationSensorManager.stop();
            }
            myAudioManager.abandonAudioFocus(null);
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

        if (audioBuffer == null) return; // FIXME: 14/01/16 is this necessary?
        if (signalListener != null) signalListener.signalChanged(audioBuffer);

        audioProcessor.processSamples(sampleCounter, audioBuffer);
        Float volume = volumeAdjust.newVolume(audioBuffer, true); // fixme: rotationDetected
        if (volume != null) setVolume(volume);

        sampleCounter += audioBuffer.length;
    }

    private float windSpeed(float freq) {
        return freq > 0 ? freq*0.325f + 0.2f : 0f;
    }

    @Override
    public void newRotation(Rotation rotation) {

        volumeAdjust.newRotation(rotation.time);

        rotationProcessor.newRotation(rotation);

        // send to upwards
        if (speedListener != null) {
            float windspeed = windSpeed(sampleRate/(float) rotation.timeOneRotation);
            long eventTime = startTime + (int) (1000*rotation.time/(float) sampleRate);
            if (throttleSpeed.shouldSend(eventTime)) speedListener.speedChanged(new SpeedEvent(eventTime, windspeed));
        }
    }

    public void setVolume(float vol) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            player.setStereoVolume(vol, vol);
        } else {
            player.setVolume(vol);
        }
    }

    @Override
    public void newDirection(Direction direction) {
//        Log.d("TAG", direction.globalDirection);
    }
}
