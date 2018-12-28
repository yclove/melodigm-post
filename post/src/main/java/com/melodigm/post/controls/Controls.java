package com.melodigm.post.controls;

import android.content.Context;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.service.MusicService;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.RandomUtil;
import com.melodigm.post.util.SPUtil;

public class Controls {
	public static void playControl(Context context) {
		sendMessage(context.getResources().getString(R.string.play));
	}

	public static void pauseControl(Context context) {
		sendMessage(context.getResources().getString(R.string.pause));
	}

	public static void nextControl(Context context) {
		boolean isServiceRunning = DeviceUtil.isServiceRunning(MusicService.class.getName(), context);
		if (!isServiceRunning) return;

		if (PlayerConstants.SONG_ON_AIR) {
			PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage(0, "next"));
		} else {
			if (PlayerConstants.SONGS_LIST.size() > 0 ) {
				if (PlayerConstants.SONG_RADIO) {
					PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
				} else {
					if (PlayerConstants.SONG_NUMBER < (PlayerConstants.SONGS_LIST.size() - 1)) {
						if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK) {
							for (int i = 0; i < 10; i++) {
								int randomVal = RandomUtil.getRandom(0, PlayerConstants.SONGS_LIST.size());
								if (PlayerConstants.SONG_NUMBER != randomVal) {
									PlayerConstants.SONG_NUMBER = randomVal;
									break;
								}
							}
						} else {
							PlayerConstants.SONG_NUMBER++;
						}
					} else {
						PlayerConstants.SONG_NUMBER = 0;
					}

					SPUtil.setSharedPreference(context, Constants.SP_MPS_POSITION, PlayerConstants.SONG_NUMBER);
					PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
				}
			}
		}
		PlayerConstants.SONG_PAUSED = false;
	}

	public static void previousControl(Context context) {
		boolean isServiceRunning = DeviceUtil.isServiceRunning(MusicService.class.getName(), context);
		if (!isServiceRunning) return;

		if (PlayerConstants.SONG_ON_AIR) {
			PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage(0, "prev"));
		} else {
			if (PlayerConstants.SONGS_LIST.size() > 0 ) {
				if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK) {
					for (int i = 0; i < 10; i++) {
						int randomVal = RandomUtil.getRandom(0, PlayerConstants.SONGS_LIST.size());
						if (PlayerConstants.SONG_NUMBER != randomVal) {
							PlayerConstants.SONG_NUMBER = randomVal;
							break;
						}
					}
				} else {
					if (PlayerConstants.SONG_NUMBER > 0) {
						PlayerConstants.SONG_NUMBER--;
					} else {
						PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.size() - 1;
					}
				}

				SPUtil.setSharedPreference(context, Constants.SP_MPS_POSITION, PlayerConstants.SONG_NUMBER);
				PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
			}
		}
		PlayerConstants.SONG_PAUSED = false;
	}
	
	private static void sendMessage(String message) {
		try {
			PlayerConstants.PLAY_PAUSE_HANDLER.sendMessage(PlayerConstants.PLAY_PAUSE_HANDLER.obtainMessage(0, message));
		} catch(Exception e) {}
	}
}
