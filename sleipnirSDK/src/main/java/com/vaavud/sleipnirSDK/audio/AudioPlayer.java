package com.vaavud.sleipnirSDK.audio;

import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import java.io.FileOutputStream;


public class AudioPlayer extends Thread {

    private static final String TAG = "SDK:AudioPlayer";

    private AudioTrack audioTrack;
    private double offset;
    private boolean isPlaying;

    private final int duration = 1; // seconds
    private final int sampleRate = 44100; //Hz
    private final int numSamples = duration * sampleRate;
    private short sample[] = new short[numSamples * 2];
    private final double freqOfTone = 14700; // hz

//		private FileOutputStream os;

    public AudioPlayer(AudioTrack player) {
        audioTrack = player;

        offset = Math.PI;

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
            while (isPlaying && audioTrack != null) {
                if (sample != null) {
                    audioTrack.write(sample, 0, sample.length);
                }
            }
        } else {
//						Log.d(TAG, "Player not Initialized " + audioTrack.getState());
        }
    }

    /**
     * Called from outside of the thread in order to stop the playback loop
     */
    public void close() {
        if (audioTrack != null && audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.flush();
            audioTrack.stop();
            audioTrack.release();
        }
        isPlaying = false;
        sample = null;
    }

//		//convert short to byte
//		private byte[] short2byte(short[] sData) {
//				int shortArrsize = sData.length;
//				byte[] bytes = new byte[shortArrsize * 2];
//				for (int i = 0; i < shortArrsize; i++) {
//						bytes[i * 2] = (byte) (sData[i] & 0x00FF);
//						bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
////		        sData[i] = 0;
//				}
//				return bytes;
//		}

    public void setVolume(float vol) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setStereoVolume(vol, vol);
        } else {
            audioTrack.setVolume(vol);
        }

        Log.d(TAG, "volume:" + vol);

    }


}
