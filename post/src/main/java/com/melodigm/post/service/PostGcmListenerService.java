package com.melodigm.post.service;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;

public class PostGcmListenerService extends GcmListenerService {
    /**
     *
     * @param from SenderID 값을 받아온다.
     * @param data Set형태로 GCM으로 받은 데이터 payload이다.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        LogUtil.e("GCM ▶ 메시지 수신(" + from + ") : " + data);

        // GCM 으로 받은 메세지를 디바이스에 알려주는 sendNotification()을 호출한다.
        DeviceUtil.sendNotification(this, data);
    }
}
