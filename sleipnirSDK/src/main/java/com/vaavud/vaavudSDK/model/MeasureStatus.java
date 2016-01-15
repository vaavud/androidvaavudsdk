package com.vaavud.vaavudSDK.model;

import com.vaavud.vaavudSDK.R;

/**
 * Created by juan on 14/01/16.
 */

public enum MeasureStatus {
		MEASURING(R.string.info_measuring),
		NO_SIGNAL(R.string.info_no_signal),
		NO_AUDIO_SIGNAL(R.string.info_no_audio_signal),
		KEEP_VERTICAL(R.string.info_keep_steady);

		private int id;

		private MeasureStatus(int id) {
				this.id = id;
		}

		public int getResourceId() {
				return id;
		}
}