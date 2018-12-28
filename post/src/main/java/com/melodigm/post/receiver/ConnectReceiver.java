package com.melodigm.post.receiver;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;

public class ConnectReceiver extends android.content.BroadcastReceiver {
	private Context mContext;
	private ConnectivityManager mConnectivityManager;
	private NetworkInfo mNetworkInfo;

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.e("ConnectReceiver > onReceive ▶ " + intent.getAction());
		mContext = context;

		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI && mNetworkInfo.isConnectedOrConnecting()) {
					LogUtil.e("ConnectReceiver ▶ WIFI 연결");
					PlayerConstants.MOBILE_NETWORK = false;
				} else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE && mNetworkInfo.isConnectedOrConnecting()) {
					LogUtil.e("ConnectReceiver ▶ 모바일 네트워크 연결");
					PlayerConstants.MOBILE_NETWORK = true;
				} else {
					LogUtil.e("ConnectReceiver ▶ 네트워크 오프라인");
					PlayerConstants.MOBILE_NETWORK = false;
				}
			} else {
				LogUtil.e("ConnectReceiver ▶ 네트워크 오프라인");
				PlayerConstants.MOBILE_NETWORK = false;
			}
		}
	}
}
