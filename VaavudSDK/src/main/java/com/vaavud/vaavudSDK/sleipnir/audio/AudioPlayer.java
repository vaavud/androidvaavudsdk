package com.vaavud.vaavudSDK.sleipnir.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

public class AudioPlayer extends Thread {

    private static final String TAG = "SDK:AudioPlayer";

    private AudioTrack audioTrack;
    private boolean isPlaying;

    private final int duration = 1; // seconds
    private final int sampleRate = 44100; //Hz
    private final int numSamples = duration * sampleRate;
    private short sample[] = new short[numSamples * 2];
    private final double freqOfTone = 14700; // hz

    public AudioPlayer() {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, sampleRate * 2, AudioTrack.MODE_STREAM);

        double offset = Math.PI;

        for (int i = 0; i < numSamples * 2; i = i + 2) {
            sample[i] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate / freqOfTone))) * Short.MAX_VALUE));
            sample[i + 1] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate / freqOfTone)) + offset) * Short.MAX_VALUE));
        }

        if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
    }

    @Override
    public void run() {
        isPlaying = true;
        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            while (isPlaying) {
                if (sample != null) {
                    audioTrack.write(sample, 0, sample.length);
                }
            }
        } else {
            Log.d(TAG, "Player not Initialized " + audioTrack.getState());
        }
    }

    /**
     * Called from outside of the thread in order to stop the playback loop
     */
    public void end() {
        isPlaying = false;
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.flush();
            audioTrack.stop();
            audioTrack.release();
        } else {
            throw new RuntimeException("Woops something with playstate wrong");
        }
    }

    public void setVolume(float vol) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setStereoVolume(vol, vol);
        } else {
            audioTrack.setVolume(vol);
        }
        Log.d(TAG, "volume:" + vol);

    }
}
