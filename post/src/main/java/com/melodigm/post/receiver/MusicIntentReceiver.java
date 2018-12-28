package com.melodigm.post.receiver;

import android.content.Context;
import android.content.Intent;

import com.melodigm.post.controls.Controls;
import com.melodigm.post.util.LogUtil;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.e("MusicIntentReceiver Action : " + intent.getAction());
		mContext = context;

		if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			LogUtil.e("Headphones disconnected.");
			Controls.pauseControl(mContext);
		}
	}
}
