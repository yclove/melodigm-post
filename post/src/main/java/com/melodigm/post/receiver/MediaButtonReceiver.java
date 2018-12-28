package com.melodigm.post.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.KeyEvent;

import com.melodigm.post.R;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;

public class MediaButtonReceiver extends BroadcastReceiver {
	private Context mContext;
	private long DOUBLE_CLICK_DELAY = 500;
	private static int clickCount = 0;

	public MediaButtonReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;

		String intentAction = intent.getAction();
		if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			return;
		}

		KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
		if (event == null) {
			return;
		}

		int action = event.getAction();
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
				if (action == KeyEvent.ACTION_DOWN) {
					clickCount++;
					Handler handler = new Handler();
					Runnable r = new Runnable() {
						@Override
						public void run() {
							if (clickCount == 1) {
								LogUtil.e("MediaButtonReceiver ▶ single click");
								final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.single_click);
								mp.start();

								if (PlayerConstants.SONG_PAUSED)
									Controls.playControl(mContext);
								else
									Controls.pauseControl(mContext);
							}

							if (clickCount == 2) {
								LogUtil.e("MediaButtonReceiver ▶ double click");
								final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.double_click);
								mp.start();

								Controls.nextControl(mContext);
							}

							if (clickCount == 3) {
								LogUtil.e("MediaButtonReceiver ▶ triple click");
								final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.triple_click);
								mp.start();

								Controls.previousControl(mContext);
							}

							clickCount = 0;
						}
					};
					if (clickCount == 1) {
						handler.postDelayed(r, DOUBLE_CLICK_DELAY);
					}
				}
				break;
		}

		// 수신한 Broadcast를 지움으로써 다른 앱에서 인텐트 또 받아서 수행되는거 방지
		if (isOrderedBroadcast())
			abortBroadcast();
	}
}
