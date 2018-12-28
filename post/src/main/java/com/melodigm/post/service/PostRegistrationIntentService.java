package com.melodigm.post.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;

import java.io.IOException;

public class PostRegistrationIntentService extends IntentService {

    private static final String TAG = "PostRegistrationIntentService";

    public PostRegistrationIntentService() {
        super(TAG);
    }

    /**
     * GCM 을 위한 Instance ID의 토큰을 생성하여 가져온다.
     * @param intent
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        // GCM을 위한 Instance ID를 가져온다.
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            synchronized (TAG) {
                // GCM 앱을 등록하고 획득한 설정파일인 google-services.json 을 기반으로 SenderID를 자동으로 가져온다.
                String default_senderId = getString(R.string.gcm_defaultSenderId);
                // GCM 기본 scope 는 "GCM"이다.
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                // Instance ID에 해당하는 토큰을 생성하여 가져온다.
                String token = instanceID.getToken(default_senderId, scope, null);

                SPUtil.setSharedPreference(this, Constants.SP_PUSH_ID, token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
