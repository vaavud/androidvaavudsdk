package com.vaavud.sleipnirSDK.wind;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;

import com.vaavud.sleipnirSDK.OrientationController;
import com.vaavud.sleipnirSDK.R;
import com.vaavud.sleipnirSDK.SettingsContentObserver;
import com.vaavud.sleipnirSDK.audio.AudioPlayer;
import com.vaavud.sleipnirSDK.audio.AudioRecorder;
import com.vaavud.sleipnirSDK.audio.VolumeAdjust;
import com.vaavud.sleipnirSDK.listener.AudioListener;
import com.vaavud.sleipnirSDK.listener.SignalListener;
import com.vaavud.sleipnirSDK.listener.SpeedListener;
import com.vaavud.sleipnirSDK.model.SpeedEvent;


public class SleipnirWindController implements AudioListener, RotationReceiver, DirectionReceiver {

    private static final String TAG = "SDK:SleipnirWC";

    private Context mContext;
    private Context appContext;

    private OrientationController oriCont;

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
    public SpeedListener speedListener;
    public SignalListener signalListener;

    private SettingsContentObserver mSettingsContentObserver;
    private SharedPreferences preferences;

    private Throttle throttleSpeed = new Throttle(200);

    public SleipnirWindController(Context context) {
        mContext = context;
        appContext = mContext.getApplicationContext();
        preferences = appContext.getSharedPreferences("SleipnirSDKPreferences", Context.MODE_PRIVATE);
    }

    public void startMeasuring() {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(false);

        mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
        appContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver);


        startTime = System.currentTimeMillis();
        sampleCounter = 0;

        checkVolume();

        oriCont = new OrientationController(mContext);
        if (oriCont.isSensorAvailable()) {
            oriCont.start();
        }

        audioPlayer = new AudioPlayer();
        audioRecorder = new AudioRecorder(this, processBufferSize);
        volumeAdjust = new VolumeAdjust(processBufferSize, audioRecorder.getBufferSize(), preferences.getFloat("playerVolume", 0.5f));
        audioPlayer.setVolume(volumeAdjust.getVolume());

        rotationProcessor = new RotationProcessor(this);
        tickProcessor = new TickProcessor(this);
        audioProcessor = new AudioProcessor(tickProcessor, processBufferSize);

        audioPlayer.start();
        audioRecorder.start();
    }

    public void stopMeasuring() {

        audioPlayer.end();
        audioRecorder.end();

        if (oriCont != null && oriCont.isSensorAvailable()) {
            oriCont.stop();
        }

        saveVolume();

        appContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        audioManager.abandonAudioFocus(null);
    }


    public double getOrientationAngle() {
        return oriCont.getAngle();
    }

    @Override
    public void newAudioBuffer(final short[] audioBuffer) {

        if (audioBuffer == null) return; // FIXME: 14/01/16 is this necessary?
        if (signalListener != null) signalListener.signalChanged(audioBuffer);

        audioProcessor.processSamples(sampleCounter, audioBuffer); // should be called before new Volume
        Float volume = volumeAdjust.newVolume(audioBuffer);
        if (volume != null) audioPlayer.setVolume(volume);

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
}