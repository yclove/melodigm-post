package com.melodigm.post.util;

import android.os.Handler;

import com.melodigm.post.protocol.data.OstDataItem;

import java.util.ArrayList;

public class PlayerConstants {
	//List of Songs
	public static ArrayList<OstDataItem> SONGS_LIST = new ArrayList<>();
	//song number which is playing right now from SONGS_LIST
	public static int SONG_NUMBER = 0;
	//song is playing or paused
	public static boolean SONG_PAUSED = true;
	//song changed (next, previous)
	public static boolean SONG_CHANGED = false;
	public static boolean SONG_RADIO = false;
	public static boolean SONG_ON_AIR = false;
	public static String SONG_ON_AIR_POI;
	//handler for song changed(next, previous) defined in service(SongService)
	public static Handler SONG_CHANGE_HANDLER;
	//handler for song play/pause defined in service(SongService)
	public static Handler PLAY_PAUSE_HANDLER;
	//handler for showing song progress defined in Activities(MainActivity, AudioPlayerActivity)
	public static Handler PROGRESSBAR_HANDLER;

	public static int MPS_REPEAT = 0;
	public static int MPS_RANDOM = 0;

	public static boolean PLAYER_TIMER = false;
	public static long PLAYER_TIMER_START_TIME = 0;

	public static boolean MOBILE_NETWORK = false;
}
