package com.melodigm.post.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.melodigm.post.common.Constants;

public class SPUtil {
	public static void setSharedPreference(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
		LogUtil.e("SharedPreferences [ " + key + " ] 값이 설정 되었습니다.\n" + value);
	}
	
	public static void setSharedPreference(Context context, String key, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        /*SharedPreferences prefs = context.getSharedPreferences(Constants.SERVICE_TAG, Context.MODE_PRIVATE);*/
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
		LogUtil.e("SharedPreferences [ " + key + " ] 값이 설정 되었습니다.\n" + value);
	}
	
	public static void setSharedPreference(Context context, String key, int value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
		/*LogUtil.e("SharedPreferences [ " + key + " ] 값이 설정 되었습니다.\n" + value);*/
	}

	public static String getSharedPreference(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, "");
	}

	public static String getSharedPreference(Context context, String key, String defaultVal) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, defaultVal);
	}

	public static boolean getBooleanSharedPreference(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, false);
	}

	public static boolean getBooleanSharedPreference(Context context, String key, boolean isDefault) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, isDefault);
	}

	public static int getIntSharedPreference(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(key, 0);
	}
	
	public static void actRemoveSharedPreferences(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
		LogUtil.e("SharedPreferences [ " + key + " ] 값이 삭제 되었습니다.");
	}
	
	public static void actRemoveAllSharedPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
		LogUtil.e("SharedPreferences 의 모든 값이 삭제 되었습니다.");
	}
}
