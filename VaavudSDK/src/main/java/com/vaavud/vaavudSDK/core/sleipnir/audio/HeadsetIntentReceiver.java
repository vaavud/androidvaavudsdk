package com.vaavud.vaavudSDK.core.sleipnir.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vaavud.vaavudSDK.core.listener.PlugListener;

public class HeadsetIntentReceiver extends BroadcastReceiver {
		private String TAG = "HeadSet";
		private PlugListener listener;

		public HeadsetIntentReceiver(PlugListener _listener) {
//		Log.d(TAG, "Created");
				listener = _listener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
						int status = intent.getIntExtra("state", -1);
						boolean state = status == 1;

						int microphoneStatus = intent.getIntExtra("microphone", -1);
						boolean connectedMicrophone = microphoneStatus == 1;
						listener.onHeadsetStatusChanged(state && connectedMicrophone);
				}
		}
}
