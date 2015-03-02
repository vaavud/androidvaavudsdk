package com.vaavud.sleipnirSDK.audio;

import android.media.AudioTrack;

public class VaavudAudioPlaying extends Thread{

	private AudioTrack mPlayer;
	private double offset;
	private boolean isPlaying;
	
	private final int duration = 1; // seconds
    private final int sampleRate = 44100; //Hz
    private final int numSamples = duration * sampleRate;
    private short sample[] = new short[numSamples*2];
    private final double freqOfTone = 14700; // hz
	
	public VaavudAudioPlaying(AudioTrack player){
//		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		mPlayer = player;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
			mPlayer.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
		}else{
			mPlayer.setVolume(AudioTrack.getMaxVolume());
		}
		
		if (mPlayer != null && mPlayer.getState() != AudioTrack.STATE_UNINITIALIZED ) {
            if (mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            	mPlayer.stop();
            }
        }
      
    	offset = Math.PI;
      
        for (int i = 0; i < numSamples*2; i=i+2) {
            sample[i] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate/freqOfTone))) * Short.MAX_VALUE));
            sample[i+1] = (short) ((Math.sin((2 * Math.PI * i / (sampleRate/freqOfTone))+offset) * Short.MAX_VALUE));
        }
	}
	
	@Override
    public void run()
    { 
        isPlaying=true;
        if (mPlayer.getState() == AudioTrack.STATE_INITIALIZED){
        	mPlayer.play();
	        while (isPlaying) {
	        	mPlayer.write(sample, 0, sample.length);
	        }
//	        Log.d("AudioPlayer","Stop");
        }
    }
	
	/**
     * Called from outside of the thread in order to stop the playback loop
     */
	public void close()
    { 
		isPlaying = false;
		sample = null;
		if (mPlayer!=null && mPlayer.getState() == AudioTrack.PLAYSTATE_PLAYING){
			mPlayer.flush();
			mPlayer.stop();
		}
//		player.release();
    }
	
	
	

}
