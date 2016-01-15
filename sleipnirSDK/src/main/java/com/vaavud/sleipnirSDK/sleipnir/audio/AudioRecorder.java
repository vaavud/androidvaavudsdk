package com.vaavud.sleipnirSDK.sleipnir.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.vaavud.sleipnirSDK.sleipnir.listener.AudioListener;


public class AudioRecorder extends Thread {
    private static final String TAG = "SDK:AudioRecorder";

    private boolean stopped = false;
    private AudioListener audioListener;
    private int inputBufferSize;
    private int processBufferSize;
    private AudioRecord audioRecord;
    private short[] buffer;
    private int bytesRead = 0;
    private final int sampleRate = 44100; //Hz

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public AudioRecorder(AudioListener audioListener, int processBufferSize) {
        this.processBufferSize = processBufferSize;
        this.audioListener = audioListener;

        inputBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (inputBufferSize < 3 * sampleRate) inputBufferSize = 3 * sampleRate;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, inputBufferSize);

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        if (audioRecord != null && audioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
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
        audioRecord.startRecording();
        buffer = new short[processBufferSize];
        /*
         * Loops until something outside of this thread stops it.
         * Reads the data from the recorder and writes it to the audio track for playback.
         */
        Log.d(TAG, "Recording status: " + audioRecord.getRecordingState());
        while (!stopped) {
            try {
                bytesRead = audioRecord.read(buffer, 0, buffer.length);
                if (audioListener != null && bytesRead > 0) {
                    audioListener.newAudioBuffer(buffer);
                }
            } catch (Throwable x) {
                Log.w(TAG, "Error reading voice audio" + x.getMessage());
                stopped = true;
            }
        }
    }


    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    public void end() {
        stopped = true;
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
            audioRecord.release();
        } else {
            throw new RuntimeException("Woops ");
        }
    }

    public int getBufferSize() {
        return inputBufferSize;
    }
}
