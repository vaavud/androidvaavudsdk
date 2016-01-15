package com.vaavud.vaavudSDK.sleipnir;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import com.vaavud.sleipnirSDK.R;
import com.vaavud.vaavudSDK.VaavudError;
import com.vaavud.vaavudSDK.listener.HeadingListener;
import com.vaavud.vaavudSDK.orientation.OrientationController;
import com.vaavud.vaavudSDK.sleipnir.audio.VolumeObserver;
import com.vaavud.vaavudSDK.sleipnir.audio.AudioPlayer;
import com.vaavud.vaavudSDK.sleipnir.audio.AudioRecorder;
import com.vaavud.vaavudSDK.sleipnir.audio.VolumeAdjust;
import com.vaavud.vaavudSDK.sleipnir.listener.AudioListener;
import com.vaavud.vaavudSDK.sleipnir.listener.SignalListener;
import com.vaavud.vaavudSDK.listener.SpeedListener;
import com.vaavud.vaavudSDK.model.SpeedEvent;


public class SleipnirController implements AudioListener, RotationReceiver, DirectionReceiver, HeadingListener {

    private static final String TAG = "SDK:SleipnirWC";

    private Context mContext;
    private Context appContext;

    private AudioManager audioManager;

    private final int sampleRate = 44100; //Hz
    private int processBufferSize = 512;

    private long startTime;
    private long sampleCounter;


    private Float[] coefficients;

    private AudioPlayer audioPlayer;
    private AudioRecorder audioRecorder;
    private AudioProcessor audioProcessor;
    private TickProcessor tickProcessor;
    private RotationProcessor rotationProcessor;
    private VolumeAdjust volumeAdjust;
    private SpeedListener speedListener;
    private SignalListener signalListener;

    private VolumeObserver volumeObserver;
    private SharedPreferences preferences;

    private Throttle throttleSpeed = new Throttle(200);

    // debug
    private boolean volumeHigh = false;
    private long lastChange = 0;

    public SleipnirController(Context context) {
        mContext = context;
        appContext = mContext.getApplicationContext();
        preferences = appContext.getSharedPreferences("SleipnirSDKPreferences", Context.MODE_PRIVATE);
    }

    public void start() throws VaavudError {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(false);

        volumeObserver = new VolumeObserver(mContext);
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeObserver);


        startTime = System.currentTimeMillis();
        sampleCounter = 0;

        checkVolume();

        audioPlayer = new AudioPlayer();
        audioRecorder = new AudioRecorder(this, processBufferSize);
        volumeAdjust = new VolumeAdjust(processBufferSize, audioRecorder.getBufferSize(), preferences.getFloat("playerVolume", 0.5f));
        audioPlayer.setVolume(volumeAdjust.getVolume());
        // debug
        volumeHigh = false;
        audioPlayer.setVolume(0.1f);


        rotationProcessor = new RotationProcessor(this);
        tickProcessor = new TickProcessor(this);
        audioProcessor = new AudioProcessor(tickProcessor, processBufferSize);

        audioPlayer.start();
        audioRecorder.start();
    }

    public void stop() {

        audioPlayer.end();
        audioRecorder.end();

        saveVolume();

        appContext.getContentResolver().unregisterContentObserver(volumeObserver);
        audioManager.abandonAudioFocus(null);
    }


    @Override
    public void newAudioBuffer(final short[] audioBuffer) {

        if (signalListener != null) signalListener.signalChanged(audioBuffer);

//        audioProcessor.processSamples(sampleCounter, audioBuffer); // should be called before new Volume
//        Float volume = volumeAdjust.newVolume(audioBuffer);
//        if (volume != null) audioPlayer.setVolume(volume);

        for (int i = 0; i < audioBuffer.length -1; i++) {
            if (volumeHigh) {
                if (Math.abs(audioBuffer[i]) + Math.abs(audioBuffer[i+1]) > 10000) {
                    Log.d(TAG, "time to change UP: " + String.valueOf((sampleCounter + i) - lastChange) );
                    lastChange = sampleCounter + i;
                    volumeHigh = false;
                    audioPlayer.setVolume(0.1f);
                    break;
                }
            } else {
                if (Math.abs(audioBuffer[i]) + Math.abs(audioBuffer[i+1]) < 50) {
                    Log.d(TAG, "time to change Down: " + String.valueOf((sampleCounter + i) - lastChange) );
                    lastChange = sampleCounter + i;
                    volumeHigh = true;
                    audioPlayer.setVolume(0.8f);
                    break;
                }
            }
        }
        sampleCounter += audioBuffer.length;
    }

    private float windSpeed(float freq) {
        return freq > 0 ? freq * 0.325f + 0.2f : 0f;
    }

    @Override
    public void newRotation(Rotation rotation) {

        volumeAdjust.newRotation();
        rotationProcessor.newRotation(rotation);

        // send to upwards
        if (speedListener != null) {
            float windspeed = windSpeed(sampleRate / (float) rotation.timeOneRotation);
            long eventTime = startTime + (int) (1000 * rotation.time / (float) sampleRate);
            if (throttleSpeed.shouldSend(eventTime))
                speedListener.speedChanged(new SpeedEvent(eventTime, windspeed));
        }
    }

    private void setMaxVolume(int maxVolume) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private void showVolumeAlert(final int maxVolume) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setTitle(appContext.getResources().getString(R.string.sound_disclaimer_title));
        builder1.setMessage(appContext.getResources().getString(R.string.sound_disclaimer));
        builder1.setCancelable(false);
        builder1.setNeutralButton(appContext.getResources().getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        setMaxVolume(maxVolume);
                    }
                });

        AlertDialog alert = builder1.create();
        alert.show();
    }

    private void saveVolume() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("playerVolume", volumeAdjust.getVolume());
        editor.apply();
    }

    private void checkVolume() {
        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        setMaxVolume(maxVolume);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume < maxVolume) showVolumeAlert(volume);
    }

    @Override
    public void newDirection(Direction direction) {
//        Log.d("TAG", direction.globalDirection);
    }

    @Override
    public void newHeading(float heading) {
        tickProcessor.setHeading(heading);
    }

    public void setSpeedListener(SpeedListener speedListener) {
        this.speedListener = speedListener;
    }

    public void setSignalListener(SignalListener signalListener) {
        this.signalListener = signalListener;
    }
}